package com.yeoyeo.application.payment.dto;

import com.yeoyeo.domain.DateRoom;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WaitingWebhookRefundDto {
    private final String imp_uid;
    private final long refundAmount;
    private final DateRoom dateRoom;
    private final String reason;

    @Builder
    public WaitingWebhookRefundDto(String imp_uid, long refundAmount, DateRoom dateRoom, String reason) {
        this.imp_uid = imp_uid;
        this.refundAmount = refundAmount;
        this.dateRoom = dateRoom;
        this.reason = reason;
    }
}
