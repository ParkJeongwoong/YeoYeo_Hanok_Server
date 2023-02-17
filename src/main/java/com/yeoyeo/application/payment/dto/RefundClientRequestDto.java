package com.yeoyeo.application.payment.dto;

import lombok.Getter;

@Getter
public class RefundClientRequestDto {
    private String merchant_uid; // dateRoomId
    private String reason;

    public String getDateRoomId() { return this.merchant_uid.substring(0,9); }
}