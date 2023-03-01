package com.yeoyeo.application.payment.service.eventLoop;

import com.yeoyeo.application.payment.dto.WaitingWebhookDto;
import com.yeoyeo.application.payment.dto.WaitingWebhookRefundDto;
import com.yeoyeo.application.payment.etc.exception.PaymentException;
import com.yeoyeo.application.payment.repository.PaymentRepository;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.sms.service.SmsService;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventLoop extends Thread {

    private static final long SLEEP_INTERVAL_BY_MINUTE = 360;

    private final WaitingWebhookQueue waitingWebhookQueue;
    private final ReservationRepository reservationRepository;
    private final PaymentService paymentService;
    private final SmsService smsService;

    @Override
    public void run() {
        log.info("Event Loop 시작");
        log.info("Thread Cnt : {}", Thread.activeCount());
        while (true) {
            try {
                if (waitingWebhookQueue.getFirstWebhook() != null) {
                    WaitingWebhookDto waitingWebhookDto = waitingWebhookQueue.getFirstWebhook();
                    if (expirationCheck(waitingWebhookDto.getExpirationTime())) {
                        expiredWaitingWebhookProcess(waitingWebhookQueue.popFirstWebhook());
                        log.info("클라이언트 예약 대기 Webhook 수 : {}", waitingWebhookQueue.countWaitingWebhook());
                    } else {
                        Thread.sleep(getSleepTime(waitingWebhookQueue.getFirstWebhook().getExpirationTime())*1000);
                    }
                } else { // 대기 예약 없음 -> Sleep
                    Thread.sleep(SLEEP_INTERVAL_BY_MINUTE * 60000);
                }
            } catch (InterruptedException e) {
                log.info("Interrupted");
                log.info("클라이언트 예약 대기 Webhook 수 : {}", waitingWebhookQueue.countWaitingWebhook());
            }
        }
    }

    private boolean expirationCheck(LocalDateTime expirationTime) {
        return expirationTime.isBefore(LocalDateTime.now());
    }

    @Transactional
    private void expiredWaitingWebhookProcess(WaitingWebhookDto waitingWebhookDto) {
        try {
            log.info("Webhook 검증 - 결제번호 : {} / 상품번호 : {}", waitingWebhookDto.getImp_uid(), waitingWebhookDto.getMerchant_uid());
                String imp_uid = waitingWebhookDto.getImp_uid();
            long merchant_uid = waitingWebhookDto.getMerchant_uid();
            long payedAmount = waitingWebhookDto.getPayedAmount();
            Reservation reservation = reservationRepository.findById(waitingWebhookDto.getMerchant_uid()).orElseThrow(()->new PaymentException("존재하지 않는 방입니다."));
            if (reservation == null) {
                WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
                        .imp_uid(imp_uid).refundAmount(payedAmount).reason("예약 정보가 없는 결제 발생").build();
                paymentService.refund(refundDto);
                return;
            }
            Payment payment = reservation.getPayment();
            if (reservation.getReservationState() != 1 || payment == null) {
                WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
                        .imp_uid(imp_uid).refundAmount(payedAmount).reservation(reservation).reason("이미 예약된 방에 결제 발생").build();
                paymentService.refund(refundDto);
                return;
            }
            if (!Objects.equals(reservation.getId(), merchant_uid)) {
                WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
                        .imp_uid(imp_uid).refundAmount(payedAmount).reservation(reservation).reason("잘못된 결제 발생").build();
                paymentService.refund(refundDto);
                return;
            }
            if (reservation.getTotalPrice() != payedAmount) {
                WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
                        .imp_uid(imp_uid).refundAmount(payedAmount).reservation(reservation).reason("결제 금액과 상품 가격 불일치").build();
                paymentService.refund(refundDto);
                return;
            }
            log.info("정상 예약 처리된 결제입니다.");
        } catch (PaymentException paymentException) {
            log.error("결제 취소 중 에러가 발생했습니다. 확인 바랍니다.", paymentException);
            // Todo - 에러 발생 문자 전송
        }
    }

    private long getSleepTime(LocalDateTime targetTime) {
        Duration duration = Duration.between(LocalDateTime.now(), targetTime);
        log.info("Sleep for {}s", duration.getSeconds()+1);
        return duration.getSeconds()+1;
    }

}
