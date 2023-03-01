package com.yeoyeo.application.payment.dto;

import lombok.Getter;

@Getter
public class RefundAdminRequestDto {
    private String merchant_uid; // dateRoomId
    private long cancel_request_amount;
    private String reason;

    public String getId() { return this.merchant_uid.substring(0,9); }
}
