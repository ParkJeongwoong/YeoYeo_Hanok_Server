package com.yeoyeo.application.payment.dto;

import lombok.Getter;

@Getter
public class RefundRequestDto {
    private String merchant_uid; // dateRoomId
    private long cancel_request_amount;
    private String reason;
}
