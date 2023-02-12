package com.yeoyeo.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class RefundServerRequestDto {
    private final String reason;
    private final String imp_uid;
    private final long amount;
    private final Integer checksum;

    @Builder
    public RefundServerRequestDto(String reason, String imp_uid, long cancel_request_amount, Integer cancelableAmount) {
        this.reason = reason; // 클라이언트로부터 받은 환불사유
        this.imp_uid = imp_uid;
        this.amount = cancel_request_amount; // 클라이언트로부터 받은 환불금액
        this.checksum = cancelableAmount; // [권장] 환불 가능 금액 입력
    }
}
