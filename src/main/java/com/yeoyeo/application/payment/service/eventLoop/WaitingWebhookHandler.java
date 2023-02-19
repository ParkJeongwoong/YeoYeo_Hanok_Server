package com.yeoyeo.application.payment.service.eventLoop;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.payment.dto.WaitingWebhookDto;
import com.yeoyeo.application.payment.etc.exception.WaitingWebhookException;
import com.yeoyeo.application.payment.repository.PaymentRepository;
import com.yeoyeo.application.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitingWebhookHandler {

    private static final long EXPIRATION_TIME = 1;

    private final WaitingWebhookQueue waitingWebhookQueue;
    private WaitingWebhookLoop waitingWebhookLoop;

    private final PaymentRepository paymentRepository;
    private final DateRoomRepository dateRoomRepository;
    private final PaymentService paymentService;

    @PostConstruct
    private void run() {
        this.waitingWebhookLoop = new WaitingWebhookLoop(waitingWebhookQueue, paymentRepository, dateRoomRepository, paymentService);
        waitingWebhookLoop.start();
    }

    public void add(String imp_uid, String merchant_uid, long payedAmount, String dateRoomId) {
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(EXPIRATION_TIME);
        WaitingWebhookDto waitingWebhookDto = WaitingWebhookDto.builder()
                .imp_uid(imp_uid).merchant_uid(merchant_uid)
                .payedAmount(payedAmount).dateRoomId(dateRoomId)
                .expirationTime(expirationTime).build();
        if (!this.waitingWebhookQueue.add(waitingWebhookDto)) {
            log.error("미결제 예약 대기열 추가 실패", new WaitingWebhookException("미결제 예약 대기열 추가 실패"));
        }
        log.info("상품번호 : {} / 예약기한 : {}", merchant_uid, expirationTime);
        this.waitingWebhookLoop.interrupt();
    }

}
