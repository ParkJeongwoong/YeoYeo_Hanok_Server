//package com.yeoyeo.application.payment.service.eventLoop;
//
//import com.yeoyeo.application.payment.dto.WaitingWebhookDto;
//import com.yeoyeo.application.payment.etc.exception.WaitingWebhookException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//import java.time.LocalDateTime;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class EventLoopHandler {
//
//    private static final long EXPIRATION_TIME = 1;
//
//    private final WaitingWebhookQueue waitingWebhookQueue;
//    private final EventLoop eventLoop;
//
//    @PostConstruct
//    private void run() {
//        eventLoop.start();
//    }
//
//    public void addWaitingWebHook(String imp_uid, long merchant_uid, long payedAmount) {
//        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(EXPIRATION_TIME);
//        WaitingWebhookDto waitingWebhookDto = WaitingWebhookDto.builder()
//                .imp_uid(imp_uid).merchant_uid(merchant_uid)
//                .payedAmount(payedAmount).expirationTime(expirationTime).build();
//        if (!this.waitingWebhookQueue.add(waitingWebhookDto)) {
//            log.error("미결제 예약 대기열 추가 실패", new WaitingWebhookException("미결제 예약 대기열 추가 실패"));
//        }
//        log.info("상품번호 : {} / 예약기한 : {}", merchant_uid, expirationTime);
//        this.eventLoop.interrupt();
//    }
//
//    public int getWaitingWebHookQueueSize() { return waitingWebhookQueue.countWaitingWebhook(); }
//
//}
