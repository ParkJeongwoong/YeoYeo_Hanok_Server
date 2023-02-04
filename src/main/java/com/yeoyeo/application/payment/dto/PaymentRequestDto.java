package com.yeoyeo.application.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    // 아임포트 정보
    private String imp_uid;
    private String merchant_uid; // dateRoomId
}
