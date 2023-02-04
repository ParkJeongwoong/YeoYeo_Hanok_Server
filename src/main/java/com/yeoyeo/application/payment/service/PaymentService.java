package com.yeoyeo.application.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.guest.repository.GuestRepository;
import com.yeoyeo.application.payment.dto.ImpTokenRequestDto;
import com.yeoyeo.application.payment.dto.ImpTokenResponseDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.etc.exception.PaymentException;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.DateRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final DateRoomRepository dateRoomRepository;
    private final GuestRepository guestRepository;

    @Value("${payment.iamport.key}")
    String imp_key;
    @Value("${payment.iamport.secret}")
    String imp_secret;

    String IMP_GET_TOKEN_URL = "https://api.iamport.kr/users/getToken";
    String IMP_GET_PAYMENT_DATA_URL = "https://api.iamport.kr/payments/";

    private WebClient WebClient(String contentType) {
        return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, contentType).build();
    }

    public GeneralResponseDto pay(PaymentRequestDto requestDto) {
        try {
//            Reservation reservation = reservationRepository.findById(requestDto.getReservationId()).orElseThrow(NoSuchElementException::new);
            DateRoom dateRoom = dateRoomRepository.findById(requestDto.getMerchant_uid()).orElseThrow(NoSuchElementException::new);
//            Guest guest = guestRepository.findById(requestDto.getGuestId()).orElseThrow(NoSuchElementException::new);

            // 결제 정보 조회
            log.info("imp_uid : {}", requestDto.getImp_uid());
            log.info("merchant_uid : {}", requestDto.getMerchant_uid());
            Map<String, Object> paymentData = getPaymentData(requestDto.getImp_uid());

            // 결제 검증
            validatePayment(dateRoom, paymentData);
            completeReservation(dateRoom);

            return GeneralResponseDto.builder()
                    .successYN("Y")
                    .message("예약이 확정되었습니다.")
                    .build();
        }
        catch (RoomReservationException dateRoomException) {
            log.error("방날짜 예약 완료 오류", dateRoomException);
            return GeneralResponseDto.builder()
                    .successYN("N")
                    .message("예약 완료 작업 중 오류가 발생했습니다.")
                    .build();
        }
        catch (ReservationException reservationException) {
            log.error("예약 완료 오류", reservationException);
            return GeneralResponseDto.builder()
                    .successYN("N")
                    .message("예약 완료 작업 중 오류가 발생했습니다.")
                    .build();
        }
        catch (PaymentException paymentException) {
            log.error("결제 오류", paymentException);
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

    private void completeReservation(DateRoom dateRoom) {
        log.info("결제 완료");
        dateRoom.setStateBooked();
//            reservation.setStatePayed();

        // Todo : 결제 완료 문자 전송
    }

}
