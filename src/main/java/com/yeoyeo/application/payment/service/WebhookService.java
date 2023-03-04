/*
Reservation - Payment를 분리하면서 Webhook 쪽에서도 굳이 Client의 Reservation을 기다릴 필요가 없어짐
-> Webhook, Client 둘 중 먼저 도착하는 쪽으로 예약 확정
-> EventLoop 방식이 더이상 필요 없어짐 & Payment 쪽에 이미 Payment가 생성됏는지 확인하는 로직 추가
*/

//package com.yeoyeo.application.payment.service;
//
//import com.yeoyeo.application.common.dto.GeneralResponseDto;
//import com.yeoyeo.application.payment.dto.ImpWebHookDto;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Slf4j
//@RequiredArgsConstructor
//@Service
//public class WebhookService {
//
//    @Transactional
//    public GeneralResponseDto webhook(ImpWebHookDto webHookDto) {
//
//        try {
//            log.info("Webhook 수신 : {} / {}", webHookDto.getImp_uid(), webHookDto.getMerchant_uid());
//            Reservation reservation = reservationRepository.findById(webHookDto.getMerchant_uid()).orElseThrow(NoSuchElementException::new);
//            String accessToken = paymentService.getToken();
//            Map<String, Object> paymentData = paymentService.getPaymentData(webHookDto.getImp_uid(), accessToken);
//            paymentService.validatePayment(reservation, paymentData, accessToken);
//            validateWebHook(reservation, paymentData);
//            return true;
//        } catch (PaymentException paymentException) {
//            log.error("Webhook 결제 데이터 검증 중 문제가 발생했습니다.", paymentException);
//            return false;
//        }
//
//    }
//
//    public Integer checkWebhook() { return eventLoopHandler.getWaitingWebHookQueueSize(); }
//
//    @Transactional
//    private void validateWebHook(Reservation reservation, Map<String, Object> paymentData) throws PaymentException {
//        // Webhook이 나중에 수신되는 경우 -> payment 체크 -> 끝
//        // Webhook이 먼저 수신되는 경우 -> 1분 대기 -> payment 재확인
//        // Webhook만 수신 되는 경우 -> 1분 대기 후 환불
//        String imp_uid = paymentData.get("imp_uid").toString();
//        long merchant_uid = Long.parseLong(paymentData.get("merchant_uid").toString());
//        long payedAmount = (long) (Integer) paymentData.get("amount");
//
//        try {
//            if (reservation.getPayment() == null) {
//                // 조건 1. 데이터 생성 X -> 1분 대기
//                for (DateRoom dateRoom:reservation.getDateRoomList()) dateRoom.setStateWaiting();
//                eventLoopHandler.addWaitingWebHook(imp_uid, merchant_uid, payedAmount);
//                reservationRepository.save(reservation);
//                return;
//            }
//            if (String.valueOf(reservation.getId()).equals(merchant_uid) && reservation.getTotalPrice() == payedAmount) {
//                // 조건 2. 검증 완료 (예약이 완료된 후 올바른 webhook 도착)
//                return;
//            }
//            // 예약이 완료된 후 잘못된 webhook 도착
//            WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
//                    .imp_uid(imp_uid).refundAmount(payedAmount).reservation(reservation).reason("잘못된 결제 발생").build();
//            paymentService.refund(refundDto);
//            throw new PaymentException("예약되지 않은 결제 정보가 수신됐습니다.");
//        } catch (RoomReservationException roomReservationException) {
//            WaitingWebhookRefundDto refundDto = WaitingWebhookRefundDto.builder()
//                    .imp_uid(imp_uid).refundAmount(payedAmount).reservation(reservation).reason("이미 예약된 방에 결제 발생").build();
//            paymentService.refund(refundDto);
//            throw new PaymentException(roomReservationException.getMessage());
//        }
//    }
//
//}
