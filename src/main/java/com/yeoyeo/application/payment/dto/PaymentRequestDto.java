package com.yeoyeo.application.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    // 아임포트 정보
    private String imp_uid;
    private long merchant_uid; // reservationId

    public PaymentRequestDto(String imp_uid, long merchant_uid) {
        this.imp_uid = imp_uid;
        this.merchant_uid = merchant_uid;
    }
}
