package com.yeoyeo.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WaitingWebhookDto {
    private final String imp_uid;
    private final String merchant_uid;
    private final long payedAmount;
    private final String dateRoomId;
    private final LocalDateTime expirationTime;

    @Builder
    public WaitingWebhookDto(String imp_uid, String merchant_uid, long payedAmount, String dateRoomId, LocalDateTime expirationTime) {
        this.imp_uid = imp_uid;
        this.merchant_uid = merchant_uid;
        this.payedAmount = payedAmount;
        this.dateRoomId = dateRoomId;
        this.expirationTime = expirationTime;
    }
}
