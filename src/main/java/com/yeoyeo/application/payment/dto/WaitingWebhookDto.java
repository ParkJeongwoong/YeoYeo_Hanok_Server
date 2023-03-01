package com.yeoyeo.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WaitingWebhookDto {
    private final String imp_uid;
    private final long merchant_uid;
    private final long payedAmount;
    private final LocalDateTime expirationTime;

    @Builder
    public WaitingWebhookDto(String imp_uid, long merchant_uid, long payedAmount, LocalDateTime expirationTime) {
        this.imp_uid = imp_uid;
        this.merchant_uid = merchant_uid;
        this.payedAmount = payedAmount;
        this.expirationTime = expirationTime;
    }
}
