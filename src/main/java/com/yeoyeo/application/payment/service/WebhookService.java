package com.yeoyeo.application.payment.service;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.payment.dto.ImpWebHookDto;
import com.yeoyeo.application.payment.dto.WaitingWebhookRefundDto;
import com.yeoyeo.application.payment.etc.exception.PaymentException;
import com.yeoyeo.application.payment.repository.PaymentRepository;
import com.yeoyeo.application.payment.service.eventLoop.WaitingWebhookHandler;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class WebhookService {

    private final DateRoomRepository dateRoomRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final WaitingWebhookHandler waitingWebhookHandler;

    @Transactional
    public void webhook(ImpWebHookDto webHookDto) {
        try {
            log.info("Webhook 수신 : {} / {}", webHookDto.getImp_uid(), webHookDto.getMerchant_uid());
            DateRoom dateRoom = dateRoomRepository.findById(webHookDto.getDateRoomId()).orElseThrow(NoSuchElementException::new);
            Payment payment = paymentRepository.findByMerchantUid(webHookDto.getMerchant_uid());
            String accessToken = paymentService.getToken();
            Map<String, Object> paymentData = paymentService.getPaymentData(webHookDto.getImp_uid(), accessToken);
            validateWebHook(dateRoom, paymentData, payment);
        } catch (PaymentException paymentException) {
            log.error("Webhook 결제 데이터 검증 중 문제가 발생했습니다.", paymentException);
        }
    }

    public Integer checkWebhook() { return waitingWebhookHandler.getQueueSize(); }

    @Transactional
    private void validateWebHook(DateRoom dateRoom, Map<String, Object> paymentData, Payment payment) throws PaymentException {
        // Webhook이 나중에 수신되는 경우 -> payment 체크 -> 끝
        // Webhook이 먼저 수신되는 경우 -> 1분 대기 -> payment 재확인
        // Webhook만 수신 되는 경우 -> 1분 대기 후 환불
        String imp_uid = paymentData.get("imp_uid").toString();
        String merchant_uid = paymentData.get("merchant_uid").toString();
        long payedAmount = (long) (Integer) paymentData.get("amount");

        try {
            if (payment == null) {
                // 조건 1. 데이터 생성 X -> 1분 대기
                dateRoom.setStateWaiting();
                waitingWebhookHandler.add(imp_uid, merchant_uid, payedAmount, dateRoom.getDateRoomId());
                dateRoomRepository.save(dateRoom);
                return;
            }
            if (payment.getMerchantUid().equals(merchant_uid)) {
                // 조건 2. 검증 완료 (예약이 완료된 후 올바른 webhook 도착)
                return;
            }
            // 예약이 완료된 후 잘못된 webhook 도착
            WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
                    .imp_uid(imp_uid).refundAmount(payedAmount).dateRoom(dateRoom).reason("잘못된 결제 발생").build();
            paymentService.refund(refundDto);
            throw new PaymentException("예약되지 않은 결제 정보가 수신 됐습니다.");
        } catch (RoomReservationException roomReservationException) {
            WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
                    .imp_uid(imp_uid).refundAmount(payedAmount).dateRoom(dateRoom).reason("이미 예약된 방에 결제 발생").build();
            paymentService.refund(refundDto);
            throw new PaymentException(roomReservationException.getMessage());
        }
    }
}
