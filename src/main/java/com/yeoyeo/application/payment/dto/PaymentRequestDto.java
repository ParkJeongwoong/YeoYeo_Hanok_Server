package com.yeoyeo.application.payment.dto;

import com.yeoyeo.domain.GuestHome;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    // 아임포트 정보
    private String imp_uid;
    private String merchant_uid; // dateRoomId

    // 예약자 정보
    private String name;
    private String phoneNumber;
    private String email;
    private long guestCount;
    private String request;

    public GuestHome createGuest() {
        return GuestHome.builder().name(this.name).phoneNumber(this.phoneNumber).email(this.email).guestCount(this.guestCount).request(this.request).build();
    }

    public String getDateRoomId() { return this.merchant_uid.substring(0,13); }
}
