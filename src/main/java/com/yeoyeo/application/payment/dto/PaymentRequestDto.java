package com.yeoyeo.application.payment.dto;

import com.yeoyeo.domain.GuestHome;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    // 아임포트 정보
    private String imp_uid;
    private long merchant_uid; // reservationId

    // 예약자 정보
    private String name;
    private String phoneNumber;
    private String email;
    private int guestCount;
    private String request;

}
