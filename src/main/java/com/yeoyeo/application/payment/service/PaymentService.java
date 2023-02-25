package com.yeoyeo.application.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.service.eventLoop.WaitingWebhookHandler;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.application.payment.dto.*;
import com.yeoyeo.application.payment.etc.exception.PaymentException;
import com.yeoyeo.application.payment.repository.PaymentRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestHome;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
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

    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;

    private final WebClientService webClientService;
    private final ReservationService reservationService;

    @Transactional
    public GeneralResponseDto pay(PaymentRequestDto requestDto) {
        try {
            // 결제 정보 조회
            log.info("imp_uid : {}", requestDto.getImp_uid());
            log.info("DateRoom ID : {}", requestDto.getDateRoomId());
            String accessToken = getToken();
            Map<String, Object> paymentData = getPaymentData(requestDto.getImp_uid(), accessToken);

            DateRoom dateRoom = dateRoomRepository.findById(requestDto.getDateRoomId()).orElseThrow(NoSuchElementException::new);
            GuestHome guest = requestDto.createGuest();
            Payment payment = makePayment(paymentData);

            // 결제 검증
            validatePayment(dateRoom, paymentData, accessToken);

            // 결제 완료
            completeReservation(dateRoom, guest, payment);

            return GeneralResponseDto.builder()
                    .success(true)
                    .message("예약이 확정되었습니다.")
                    .build();
        } catch (PaymentException paymentException) {
            log.error("결제 오류가 발생했습니다.", paymentException);
            return GeneralResponseDto.builder()
                    .success(false)
                    .message(paymentException.getMessage())
                    .build();
        }
    }

    @Transactional
    public GeneralResponseDto refund(RefundClientRequestDto requestDto) {
        try {
            // 결제 정보 조회
            Reservation reservation = requestDto.getValidatedReservation(reservationRepository);
            DateRoom dateRoom = reservation.getDateRoom();
            log.info("DateRoom : {}, Reservation {} {} {}", dateRoom.getDateRoomId(), reservation.getId(), reservation.getDateRoom().getDateRoomId(), reservation.getReservationState());
            Payment payment = reservation.getPayment();
            Integer cancelableAmount = payment.getCancelableAmount();
            long refundAmount = getRefundableAmount(reservation);
            log.info("Refundable Amount : {}", refundAmount);

            // 검증
            validateRefunding(refundAmount, cancelableAmount);

            // 환불 요청
            String accessToken = getToken();
            Map<String, Object> refundData = sendRefundRequest(
                    requestDto.getReason(), refundAmount, cancelableAmount, payment.getImp_uid(), accessToken);

            // 환불 완료
            payment.setCanceled(refundAmount, requestDto.getReason(), refundData.get("receipt_url").toString());
            completeRefund(dateRoom, reservation);

            return GeneralResponseDto.builder()
                    .success(true)
                    .message("환불 요청이 완료되었습니다.")
                    .build();
        } catch (PaymentException paymentException) {
            log.error("환불 오류가 발생했습니다.", paymentException);
            return GeneralResponseDto.builder()
                    .success(false)
                    .message(paymentException.getMessage())
                    .build();
        }
    }

    @Transactional
    public void refund(WaitingWebhookRefundDto refundDto) throws PaymentException {
        log.info("미예약 결제 취소 - 결제번호 : {} / 사유 : {}", refundDto.getImp_uid(), refundDto.getReason());
        // 결제 정보 조회
        DateRoom dateRoom = refundDto.getDateRoom();
        long refundAmount = refundDto.getRefundAmount();
        Integer cancelableAmount = (Integer) (int) refundAmount;

        // 환불 요청
        String accessToken = getToken();
        sendRefundRequest(refundDto.getReason(), refundAmount, cancelableAmount, refundDto.getImp_uid(), accessToken);

        // 환불 완료
        completeWebhookRefund(dateRoom);
        log.info("환불 완료");
    }

    private Map<String, Object> sendRefundRequest(String reason, long requestedAmount, Integer cancelableAmount, String imp_uid, String accessToken)
            throws PaymentException {
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
            } else if (!response.get("code").toString().equals("0")) {
                log.info(response.toString());
                throw new PaymentException(response.get("message").toString());
            } else {
                log.info("REFUND DATA : {}", response.get("response").toString());
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
            log.info("PAYMENT DATA : {}", response.get("response").toString());
            return mapper.convertValue(response.get("response"), Map.class);
        }
    }

    private void validatePayment(DateRoom dateRoom, Map<String, Object> paymentData, String accessToken) throws PaymentException {
        String status = paymentData.get("status").toString();
        String imp_uid = paymentData.get("imp_uid").toString();
        String merchant_uid = paymentData.get("merchant_uid").toString();
        long payedAmount = (long) (Integer) paymentData.get("amount");

        log.info("status : {}", status);
        log.info("amount : {}", payedAmount);
        if (!status.equals("paid")) {
            throw new PaymentException("결제가 완료되지 않았습니다.");
        }
        // Todo - Test 완료 수 검증 추가
//        if (!(dateRoom.getDateRoomId()+dateRoom.getReservationCount()).equals(merchant_uid)) {
//            sendRefundRequest("상품 번호가 유효하지 않은 결제", payedAmount, (int) payedAmount, imp_uid, accessToken);
//            throw new PaymentException("상품 번호가 유효하지 않은 결제입니다.");
//        }
        if (dateRoom.getPrice() != payedAmount) {
            sendRefundRequest("결제 금액과 상품 가격 불일치", payedAmount, (int) payedAmount, imp_uid, accessToken);
            throw new PaymentException("결제 금액이 상품 가격과 일치하지 않습니다.");
        }
    }

    @Transactional
    private void completeReservation(DateRoom dateRoom, GuestHome guest, Payment payment) throws PaymentException {
        try {
            log.info("결제가 완료되었습니다.");
            MakeReservationHomeDto reservationDto = new MakeReservationHomeDto(dateRoom, guest, payment);
            reservationService.makeReservation(reservationDto);
        } catch (ReservationException reservationException) {
            throw new PaymentException(reservationException.getMessage());
        }

        // Todo : 결제 완료 문자 전송
    }

    private Payment makePayment(Map<String, Object> paymentData) {
        return Payment.builder()
                .merchant_uid(paymentData.get("merchant_uid").toString())
                .amount((Integer) paymentData.get("amount"))
                .buyer_name(paymentData.get("buyer_name").toString())
                .buyer_tel(paymentData.get("buyer_tel").toString())
                .buyer_email(paymentData.get("buyer_email").toString())
                .buyer_addr(paymentData.get("buyer_addr").toString())
                .imp_uid(paymentData.get("imp_uid").toString())
                .pay_method(paymentData.get("pay_method").toString())
                .receipt_url(paymentData.get("receipt_url").toString())
                .status(paymentData.get("status").toString())
                .build();
    }

    private Reservation getRefundableReservation(DateRoom dateRoom) throws PaymentException {
        List<Reservation> reservationList = reservationRepository.findByDateRoom_DateRoomIdAndReservationState(dateRoom.getDateRoomId(), 1);
        log.info("Refundable Reservation Count : {}", reservationList.size());
        if (reservationList.size() == 0) throw new PaymentException("결제 정보를 찾을 수가 없습니다.");
        return reservationList.get(0);
    }

    private void validateRefunding(long requestedAmount, Integer cancelableAmount) throws PaymentException {
        if (requestedAmount <= 0) throw new PaymentException("요청된 환불금액이 없습니다.");
        if (requestedAmount > cancelableAmount) throw new PaymentException("요청된 금액이 환불가능액을 초과합니다.");
    }

    @Transactional
    private void completeRefund(DateRoom dateRoom, Reservation reservation) throws PaymentException {
        try {
            reservationService.cancel(dateRoom, reservation);
        } catch (ReservationException reservationException) {
            throw new PaymentException(reservationException.getMessage());
        }
    }

    @Transactional
    private void completeWebhookRefund(DateRoom dateRoom) throws PaymentException {
        try {
            dateRoom.resetState();
            dateRoomRepository.save(dateRoom);
        } catch (RoomReservationException roomReservationException) {
            throw new PaymentException(roomReservationException.getMessage());
        }
    }

    private long getRefundableAmount(Reservation reservation) {
        LocalDate reservationDate = reservation.getDateRoom().getDate();
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
