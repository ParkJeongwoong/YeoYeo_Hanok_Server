package com.yeoyeo.application.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.application.payment.dto.*;
import com.yeoyeo.application.payment.etc.exception.PaymentException;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    String IMP_GET_TOKEN_URL = "https://api.iamport.kr/users/getToken";
    String IMP_GET_PAYMENT_DATA_URL = "https://api.iamport.kr/payments/";
    String IMP_SET_REFUND_URL = "https://api.iamport.kr/payments/cancel";

    @Value("${payment.iamport.key}")
    String imp_key;
    @Value("${payment.iamport.secret}")
    String imp_secret;

    private final ReservationRepository reservationRepository;

    private final WebClientService webClientService;
    private final ReservationService reservationService;
    private final MessageService messageService;

    @Transactional
    public GeneralResponseDto pay(PaymentRequestDto requestDto) {
        String accessToken = getToken();
        Map<String, Object> paymentData = getPaymentData(requestDto.getImp_uid(), accessToken);
        Reservation reservation = reservationRepository.findById(requestDto.getMerchant_uid()).orElseThrow(NoSuchElementException::new);
        try {
            paymentProcess(reservation, paymentData);
            return GeneralResponseDto.builder().success(true).message("예약이 확정되었습니다.").build();
        } catch (PaymentException paymentException) {
            log.error("결제 오류가 발생했습니다. 환불 작업이 진행됩니다.", paymentException);
            String imp_uid = (String) paymentData.get("imp_uid");
            long payedAmount = (long) (Integer) paymentData.get("amount");
            accessToken = getToken();
            try {
                Map<String, Object> refundData = sendRefundRequest(paymentException.getMessage(), payedAmount, (int) payedAmount, imp_uid, accessToken);
                if (reservation.getPayment() != null) {
                    reservation.getPayment().setCanceled(payedAmount, "예약결제 증 오류 발생", (String) refundData.get("receipt_url"));
                }
                reservation.setStateRefund();
            } catch (PaymentException | ReservationException e) {
                log.error("비정상 결제 환불 중 오류 빌생", e);
                messageService.sendAdminSms("결제 오류 알림 - 비정상적인 결제에 대한 환불 작업 중 오류 발생");
            }
            return GeneralResponseDto.builder().success(false).message(paymentException.getMessage()).build();
        } catch (ReservationException reservationException) {
            log.error("결제 오류가 발생했습니다.", reservationException);
            return GeneralResponseDto.builder().success(false).message(reservationException.getMessage()).build();
        }
    }

    public GeneralResponseDto webhook(ImpWebHookDto webHookDto) {
        PaymentRequestDto requestDto = webHookDto.getPaymentRequestDto();
        String accessToken = getToken();
        Map<String, Object> paymentData = getPaymentData(requestDto.getImp_uid(), accessToken);
        Reservation reservation = reservationRepository.findById(requestDto.getMerchant_uid()).orElseThrow(NoSuchElementException::new);
        try {
            paymentProcess(reservation, paymentData);
            return GeneralResponseDto.builder().success(true).message("예약이 확정되었습니다.").build();
        } catch (ReservationException|PaymentException exception) {
            log.error("결제 오류가 발생했습니다. Webhook Process 는 환불 작업이 진행되지 않습니다.", exception);
            return GeneralResponseDto.builder().success(false).message(exception.getMessage()).build();
        }
    }

    public GeneralResponseDto confirm(ImpConfirmDto confirmDto) {
        try {
            Reservation reservation = reservationRepository.findById(confirmDto.getMerchant_uid()).orElseThrow(NoSuchElementException::new);
            if (reservation.getTotalPrice() != confirmDto.getAmount()) throw new PaymentException("결제 금액이 일치하지 않습니다.");
            List<DateRoom> dateRoomList = reservation.getDateRoomList();
            for (DateRoom dateRoom:dateRoomList) if (dateRoom.getRoomReservationState()!=0) throw new PaymentException("이미 예약이 완료된 날짜입니다.");
            return GeneralResponseDto.builder().success(true).message("결제 가능한 상태입니다.").build();
        } catch (PaymentException paymentException) {
            log.error("Confirm 검증 과정 중 실패", paymentException);
            return GeneralResponseDto.builder().success(false).message(paymentException.getMessage()).build();
        }

    }

    @Transactional
    public GeneralResponseDto refund(RefundClientRequestDto requestDto) {
        try {
            // 결제 정보 조회
            Reservation reservation = requestDto.getValidatedReservation(reservationRepository);
            Payment payment = reservation.getPayment();
            Integer cancelableAmount = payment.getCancelableAmount();
            long refundAmount = getRefundableAmount(reservation);
            log.info("Refundable Amount : {}", refundAmount);

            // 검증
            validateRefunding(refundAmount, cancelableAmount);

            // 환불 요청
            String accessToken = getToken();
            Map<String, Object> refundData = sendRefundRequest(requestDto.getReason(), refundAmount, cancelableAmount, payment.getImp_uid(), accessToken);

            // 환불 완료
            payment.setCanceled(refundAmount, requestDto.getReason(), (String) refundData.get("receipt_url"));
            completeRefund(reservation);
            log.info("고객 요청 환불 완료 (예약번호 : {})", reservation.getId());
            return GeneralResponseDto.builder().success(true).message("환불 요청이 완료되었습니다.").build();
        } catch (PaymentException paymentException) {
            log.error("환불 과정 중 오류가 발생했습니다.", paymentException);
            return GeneralResponseDto.builder().success(false).message(paymentException.getMessage()).build();
        }
    }

    @Transactional
    public GeneralResponseDto refundByAdmin(long reservationId) {
        log.info("관리자 전액 환불 및 예약 취소 - 예약번호 : {}", reservationId);
        try {
            // 결제 정보 조회
            Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
            if (reservation == null) return GeneralResponseDto.builder().success(false).message("존재하지 않은 예약번호입니다.").build();
            Payment payment = reservation.getPayment();
            if (payment == null) return GeneralResponseDto.builder().success(false).message("결제되지 않은 예약입니다.").build();
            long refundAmount = payment.getAmount();

            // 환불 요청
            String accessToken = getToken();
            sendRefundRequest("관리자의 환불 요청", refundAmount, (int) refundAmount, payment.getImp_uid(), accessToken);

            // 환불 완료
            payment.setCanceled(refundAmount, "관리자 사유 환불", "None");
            completeRefund(reservation);
            log.info("관리자 요청 환불 완료 (예약번호 : {})", reservation.getId());
            return GeneralResponseDto.builder().success(true).message("환불 요청이 완료되었습니다.").build();
        } catch (PaymentException paymentException) {
            log.error("환불 과정 중 오류가 발생했습니다.", paymentException);
            return GeneralResponseDto.builder().success(false).message(paymentException.getMessage()).build();
        }
    }

    private void paymentProcess(Reservation reservation, Map<String, Object> paymentData) throws PaymentException, ReservationException {
        if (reservation.getPayment() == null) {
            Payment payment = createPayment(paymentData, reservation);
            validatePayment(reservation, paymentData);
            completeReservation(reservation, payment);
            messageService.sendReservationSms(reservation);
        } else {
            Payment payment = reservation.getPayment();
            validatePaymentData(payment, paymentData);
        }
    }

    private Map<String, Object> sendRefundRequest(String reason, long requestedAmount, Integer cancelableAmount, String imp_uid, String accessToken) throws PaymentException {
        try {
            if (requestedAmount<=0) throw new PaymentException("환불할 금액이 없습니다.");

            RefundServerRequestDto requestDto = RefundServerRequestDto.builder()
                    .reason(reason)
                    .imp_uid(imp_uid)
                    .cancel_request_amount(requestedAmount)
                    .cancelableAmount(cancelableAmount).build();
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(requestDto);

            JSONObject response = webClientService.WebClient("application/json").post()
                    .uri(IMP_SET_REFUND_URL)
                    .header("Authorization", accessToken)
                    .body(BodyInserters.fromValue(jsonString))
                    .retrieve()
                    .bodyToMono(JSONObject.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("IAMPORT Return 데이터 문제");
            } else if (!response.get("code").equals(0)) {
                log.info("code : {}", response.get("code"));
                log.info(String.valueOf(response));
                throw new PaymentException((String) response.get("message"));
            } else {
                log.info("REFUND DATA : {}", response.get("response"));
                return mapper.convertValue(response.get("response"), Map.class);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("requestDto JSON 변환 에러");
        }
    }

    public String getToken() {
        try {
            log.info("IMP_KEY : {}", imp_key);
            log.info("IMP_SECRET : {}", imp_secret);

            ImpTokenRequestDto requestDto = ImpTokenRequestDto.builder().imp_key(imp_key).imp_secret(imp_secret).build();
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(requestDto);

            ImpTokenResponseDto response = webClientService.WebClient("application/json").post()
                                                .uri(IMP_GET_TOKEN_URL)
                                                .body(BodyInserters.fromValue(jsonString))
                                                .retrieve()
                                                .bodyToMono(ImpTokenResponseDto.class)
                                                .block();

            if (response == null) {
                throw new RuntimeException("IAMPORT Return 데이터 문제");
            } else if(response.getResponse().get("access_token") == null) {
                throw new RuntimeException("IAMPORT Return 데이터 문제 (access_token 부재)");
            } else {
                log.info("RESPONSE TOKEN : {}", response.getResponse().get("access_token"));
                return response.getResponse().get("access_token");
            }
        } catch (JsonProcessingException e) {
            log.error("requestDto JSON 변환 에러", e);
        }
        return null;
    }

    public Map<String, Object> getPaymentData(String imp_uid, String accessToken) {
        ObjectMapper mapper = new ObjectMapper();

        JSONObject response = webClientService.WebClient("application/json").get()
                .uri(IMP_GET_PAYMENT_DATA_URL+imp_uid)
                .header("Authorization", accessToken)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

        if (response == null) {
            throw new RuntimeException("IAMPORT Return 데이터 문제");
        } else if(response.get("response") == null) {
            throw new RuntimeException("IAMPORT Return 데이터 문제. (response 부재)");
        } else {
            log.info("PAYMENT DATA : {}", response.get("response"));
            return mapper.convertValue(response.get("response"), Map.class);
        }
    }

    private void validatePayment(Reservation reservation, Map<String, Object> paymentData) throws PaymentException, ReservationException {
        String status = (String) paymentData.get("status");
        String merchant_uid = (String) paymentData.get("merchant_uid");
        long payedAmount = (long) (Integer) paymentData.get("amount");

        log.info("status : {}", status);
        log.info("amount : {}", payedAmount);
        if (!status.equals("paid")) {
            throw new PaymentException("결제가 완료되지 않았습니다.");
        }
        if (!(String.valueOf(reservation.getId())).equals(merchant_uid)) {
            // 잘못된 예약 번호가 올 때 환불하는 건 다른 사람의 예약번호를 보내 다른 사람의 예약을 취소시키는 문제가 발생할 수 있음 -> 그냥 중단
            throw new ReservationException("예약 번호가 유효하지 않은 결제입니다.");
        }
        if (reservation.getTotalPrice() != payedAmount) {
            log.info("상품금액 : {} / 결제금액 : {}", reservation.getTotalPrice(), payedAmount);
            throw new PaymentException("결제 금액과 상품 가격 불일치");
        }
    }

    private void validatePaymentData(Payment payment, Map<String, Object> paymentData) throws ReservationException {
        String status = (String) paymentData.get("status");
        String imp_uid = (String) paymentData.get("imp_uid");
        long payedAmount = (long) (Integer) paymentData.get("amount");

        log.info("status : {}", status);
        log.info("amount : {}", payedAmount);
        if (status.equals("cancelled")) {
            messageService.sendAdminSms("관리자 콘솔 환불 알림 - 환불되었습니다.");
            return;
        }
        if (!status.equals("paid")) {
            messageService.sendAdminSms("결제 오류 알림 - 완료되지 않은 결제 수신. 서버 데이터 확인 필요");
            throw new ReservationException("결제가 완료되지 않았습니다.");
        }
        if (!(String.valueOf(payment.getImp_uid())).equals(imp_uid)) {
            messageService.sendAdminSms("결제 오류 알림 - 저장되지 않은 결제번호 수신. 서버 데이터 확인 필요");
            throw new ReservationException("잘못된 결제 정보입니다. 저장된 결제번호 : "+payment.getImp_uid()+" / 수신된 결제번호 : "+imp_uid);
        }
        if (payment.getAmount() != payedAmount) {
            messageService.sendAdminSms("결제 오류 알림 - 잘못된 결제금액 수신. 서버 데이터 확인 필요");
            throw new ReservationException("잘못된 결제 정보입니다. 저장된 결재금액 : "+payment.getAmount()+" / 지불된 결제금액 : "+payedAmount);
        }
    }

    @Transactional
    private void completeReservation(Reservation reservation, Payment payment) throws PaymentException {
        try {
            log.info("결제가 완료되었습니다.");
            reservationService.setReservationPaid(reservation, payment);
        } catch (ReservationException reservationException) {
            throw new PaymentException("예약 불가능한 날짜 (중복된 예약)");
        } catch (ObjectOptimisticLockingFailureException objectOptimisticLockingFailureException) {
            log.info("낙관적 락 예외 발생 - 결제 취소 처리");
            throw new PaymentException("낙관적 락 예외 발생 - 결제 취소 처리");
        }
    }

    private Payment createPayment(Map<String, Object> paymentData, Reservation reservation) {
        return Payment.builder()
                .amount((Integer) paymentData.get("amount"))
                .buyer_name((String) paymentData.get("buyer_name"))
                .buyer_tel((String) paymentData.get("buyer_tel"))
                .buyer_email((String) paymentData.get("buyer_email"))
                .buyer_addr((String) paymentData.get("buyer_addr"))
                .imp_uid((String) paymentData.get("imp_uid"))
                .pay_method((String) paymentData.get("pay_method"))
                .receipt_url((String) paymentData.get("receipt_url"))
                .status((String) paymentData.get("status"))
                .reservation(reservation)
                .build();
    }

    private void validateRefunding(long requestedAmount, Integer cancelableAmount) throws PaymentException {
        if (requestedAmount <= 0) throw new PaymentException("요청된 환불금액이 없습니다.");
        if (requestedAmount > cancelableAmount) throw new PaymentException("요청된 금액이 환불가능액을 초과합니다.");
    }

    @Transactional
    private void completeRefund(Reservation reservation) throws PaymentException {
        try {
            reservationService.cancel(reservation);
        } catch (ReservationException reservationException) {
            throw new PaymentException(reservationException.getMessage());
        }
    }

//    @Transactional
//    private void completeWebhookRefund(Reservation reservation) throws PaymentException {
//        try {
//            for (DateRoom dateRoom:reservation.getDateRoomList()) dateRoom.resetState();
//            reservationRepository.save(reservation);
//        } catch (RoomReservationException e) {
//            throw new PaymentException(e.getMessage());
//        }
//    }

    private long getRefundableAmount(Reservation reservation) {
        LocalDate reservationDate = reservation.getFirstDateRoom().getDate();
        LocalDate now = LocalDate.now();
        Period diff = Period.between(now, reservationDate);
        long paidPrice = reservation.getPayment().getAmount();

        log.info("남은 기간 : {}년 {}개월 {}일", diff.getYears(), diff.getMonths(), diff.getDays());
        if (diff.getYears() > 0 || diff.getMonths() > 0 || diff.getDays() > 10) return paidPrice;
        switch (diff.getDays()) {
            case 10: return paidPrice;
            case 9: return (long) (paidPrice*0.9);
            case 8: return (long) (paidPrice*0.8);
            case 7: return (long) (paidPrice*0.7);
            case 6: return (long) (paidPrice*0.6);
            case 5: return (long) (paidPrice*0.5);
            case 4: return (long) (paidPrice*0.4);
            case 3: return (long) (paidPrice*0.3);
            case 2: return (long) (paidPrice*0.2);
            case 1: return (long) (paidPrice*0.1);
            case 0: return 0;
        }
        return 0;
    }

}
