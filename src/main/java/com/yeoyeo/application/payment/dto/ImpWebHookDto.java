package com.yeoyeo.application.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ImpWebHookDto {
    private String imp_uid; // 결제번호
    private long merchant_uid; // 주문번호
    private String status; // 결제 결과 (paid, failed, cancelled)

    @Builder
    public ImpWebHookDto(String imp_uid, long merchant_uid, String status) {
        this.imp_uid = imp_uid;
        this.merchant_uid = merchant_uid;
        this.status = status;
    }
}
