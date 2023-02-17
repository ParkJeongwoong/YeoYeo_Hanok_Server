package com.yeoyeo.application.payment.dto;

import com.yeoyeo.domain.DateRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WaitingWebhookDto {
    private final String imp_uid;
    private final String merchant_uid;
    private final long payedAmount;
    private final DateRoom dateRoom;
    private final LocalDateTime expirationTime;

    @Builder
    public WaitingWebhookDto(String imp_uid, String merchant_uid, long payedAmount, DateRoom dateRoom, LocalDateTime expirationTime) {
        this.imp_uid = imp_uid;
        this.merchant_uid = merchant_uid;
        this.payedAmount = payedAmount;
        this.dateRoom = dateRoom;
        this.expirationTime = expirationTime;
    }
}
