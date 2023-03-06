package com.yeoyeo.application.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ImpConfirmDto {

    private String imp_uid;
    private long merchant_uid;
    private int amount;

    @Builder
    public ImpConfirmDto(String imp_uid, long merchant_uid, int amount) {
        this.imp_uid = imp_uid;
        this.merchant_uid = merchant_uid;
        this.amount = amount;
    }

}
