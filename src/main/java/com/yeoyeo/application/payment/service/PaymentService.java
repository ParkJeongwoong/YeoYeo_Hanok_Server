package com.yeoyeo.application.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.payment.dto.*;
import com.yeoyeo.application.payment.etc.exception.PaymentException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;

    private final ReservationService reservationService;

    @Value("${payment.iamport.key}")
    String imp_key;
    @Value("${payment.iamport.secret}")
    String imp_secret;

    String IMP_GET_TOKEN_URL = "https://api.iamport.kr/users/getToken";
    String IMP_GET_PAYMENT_DATA_URL = "https://api.iamport.kr/payments/";
    String IMP_SET_REFUND_URL = "https://api.iamport.kr/payments/cancel";

    private WebClient WebClient(String contentType) {
        return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, contentType).build();
    }

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
                    .successYN("Y")
                    .message("예약이 확정되었습니다.")
                    .build();
        } catch (PaymentException paymentException) {
            log.error("결제 오류가 발생했습니다.", paymentException);
            return GeneralResponseDto.builder()
                    .successYN("N")
                    .message(paymentException.getMessage())
                    .build();
        }
    }

    @Transactional
    public GeneralResponseDto refund(RefundClientRequestDto requestDto) {
        try {
            // 결제 정보 조회
            DateRoom dateRoom = dateRoomRepository.findByDateRoomId(requestDto.getDateRoomId());
            Reservation reservation = getRefundableReservation(dateRoom);
            log.info("DateRoom : {}, Reservation {} {} {}", dateRoom.getDateRoomId(), reservation.getId(), reservation.getDateRoom().getDateRoomId(), reservation.getReservationState());
            Payment payment = reservation.getPayment();
            Integer cancelableAmount = payment.getCancelableAmount();
            long requestedAmount = requestDto.getCancel_request_amount();

            // 검증
            validateRefunding(requestedAmount, cancelableAmount);

            // 환불 요청
            String accessToken = getToken();
            Map<String, Object> refundData = sendRefundRequest(
                    requestDto.getReason(), requestedAmount, cancelableAmount, payment.getImp_uid(), accessToken);

            // 환불 완료
            payment.setCanceled(requestedAmount, requestDto.getReason(), refundData.get("receipt_url").toString());
            completeRefund(dateRoom, reservation);

            return GeneralResponseDto.builder()
                    .successYN("Y")
                    .message("환불 요청이 완료되었습니다.")
                    .build();
        } catch (PaymentException paymentException) {
            log.error("환불 오류가 발생했습니다.", paymentException);
            return GeneralResponseDto.builder()
                    .successYN("N")
                    .message(paymentException.getMessage())
                    .build();
        }
    }

    private String getToken() {
        try {
            log.info("IMP_KEY : {}", imp_key);
            log.info("IMP_SECRET : {}", imp_secret);

            ImpTokenRequestDto requestDto = ImpTokenRequestDto.builder().imp_key(imp_key).imp_secret(imp_secret).build();
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(requestDto);

            ImpTokenResponseDto response = WebClient("application/json").post()
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

    private Map<String, Object> getPaymentData(String imp_uid, String accessToken) {
        ObjectMapper mapper = new ObjectMapper();

        JSONObject response = WebClient("application/json").get()
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
        String merchant_uid = paymentData.get("merchant_uid").toString();
        long payedAmount = (long) (Integer) paymentData.get("amount");

        log.info("status : {}", status);
        log.info("amount : {}", payedAmount);
        if (!status.equals("paid")) {
            throw new PaymentException("결제가 완료되지 않았습니다.");
        }
        if (!(dateRoom.getDateRoomId()+"&&"+dateRoom.getReservationCount()).equals(merchant_uid)) {
            log.info("비정상 결제 - 환불 작업이 진행됩니다.");
            sendRefundRequest("서버 결제 작업 중 오류", payedAmount, (int) payedAmount, paymentData.get("imp_uid").toString(), accessToken);
            throw new PaymentException("상품 번호가 유효하지 않은 결제입니다.");
        }
        if (dateRoom.getPrice() != payedAmount) {
            log.info("비정상 결제 - 환불 작업이 진행됩니다.");
            sendRefundRequest("서버 결제 작업 중 오류", payedAmount, (int) payedAmount, paymentData.get("imp_uid").toString(), accessToken);
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

    private Map<String, Object> sendRefundRequest(String reason, long requestedAmount, Integer cancelableAmount, String imp_uid, String accessToken)
            throws PaymentException {
        try {
            RefundServerRequestDto requestDto = RefundServerRequestDto.builder()
                    .reason(reason)
                    .imp_uid(imp_uid)
                    .cancel_request_amount(requestedAmount)
                    .cancelableAmount(cancelableAmount).build();
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(requestDto);

            JSONObject response = WebClient("application/json").post()
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

    private void completeRefund(DateRoom dateRoom, Reservation reservation) throws PaymentException {
        try {
            reservationService.cancel(dateRoom, reservation);
        } catch (ReservationException reservationException) {
            throw new PaymentException(reservationException.getMessage());
        }
    }

}
