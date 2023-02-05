package com.yeoyeo.application.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.payment.dto.ImpTokenRequestDto;
import com.yeoyeo.application.payment.dto.ImpTokenResponseDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.etc.exception.PaymentException;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestHome;
import com.yeoyeo.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final DateRoomRepository dateRoomRepository;

    private final ReservationService reservationService;

    @Value("${payment.iamport.key}")
    String imp_key;
    @Value("${payment.iamport.secret}")
    String imp_secret;

    String IMP_GET_TOKEN_URL = "https://api.iamport.kr/users/getToken";
    String IMP_GET_PAYMENT_DATA_URL = "https://api.iamport.kr/payments/";

    private WebClient WebClient(String contentType) {
        return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, contentType).build();
    }

    @Transactional
    public GeneralResponseDto pay(PaymentRequestDto requestDto) {
        try {
            // 결제 정보 조회
            log.info("imp_uid : {}", requestDto.getImp_uid());
            log.info("merchant_uid : {}", requestDto.getMerchant_uid());
            Map<String, Object> paymentData = getPaymentData(requestDto.getImp_uid());

            DateRoom dateRoom = dateRoomRepository.findById(requestDto.getMerchant_uid()).orElseThrow(NoSuchElementException::new);
            GuestHome guest = requestDto.createGuest();
            Payment payment = makePayment(paymentData);

            // 결제 검증
            validatePayment(dateRoom, paymentData);
            completeReservation(dateRoom, guest, payment);

            return GeneralResponseDto.builder()
                    .successYN("Y")
                    .message("예약이 확정되었습니다.")
                    .build();
        }
        catch (PaymentException paymentException) {
            log.error("결제 오류가 발생했습니다.", paymentException);
            return GeneralResponseDto.builder()
                    .successYN("N")
                    .message(paymentException.getMessage())
                    .build();
        }
    }

    public Map<String, Object> getPaymentData(String imp_uid) {
        String accessToken = fetchToken();
        return fetchPaymentData(imp_uid, accessToken);
    }

    private String fetchToken() {
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

            log.info("RESPONSE TOKEN : {}", response.getResponse().get("access_token"));
            return response.getResponse().get("access_token");
        } catch (JsonProcessingException e) {
            log.error("requestDto JSON 변환 에러", e);
        }
        return null;
    }

    private Map<String, Object> fetchPaymentData(String imp_uid, String accessToken) {
        ObjectMapper mapper = new ObjectMapper();

        JSONObject response = WebClient("application/json").get()
                .uri(IMP_GET_PAYMENT_DATA_URL+imp_uid)
                .header("Authorization", accessToken)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();

        log.info("PAYMENT DATA : {}", response.get("response").toString());
        return mapper.convertValue(response.get("response"), Map.class);
    }

    private void validatePayment(DateRoom dateRoom, Map<String, Object> paymentData) throws PaymentException {
        String status = paymentData.get("status").toString();
        long payedAmount = (long) (Integer) paymentData.get("amount");

        log.info("status : {}", status);
        log.info("amount : {}", payedAmount);
        if (!status.equals("paid")) throw new PaymentException("결제가 완료되지 않았습니다.");
        if (dateRoom.getPrice() != payedAmount) throw new PaymentException("결제 금액이 상품 가격과 일치하지 않습니다.");
    }

    @Transactional
    private void completeReservation(DateRoom dateRoom, GuestHome guest, Payment payment) {
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

}
