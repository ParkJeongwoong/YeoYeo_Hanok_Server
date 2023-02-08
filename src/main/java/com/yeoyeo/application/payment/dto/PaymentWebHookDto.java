package com.yeoyeo.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PaymentWebHookDto {
    private final String imp_key;
    private final String merchant_uid;
    private final String status;

    @Builder
    public PaymentWebHookDto(String imp_key, String merchant_uid, String status) {
        this.imp_key = imp_key;
        this.merchant_uid = merchant_uid;
        this.status = status;
    }
}
