package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.Guest;
import com.yeoyeo.domain.GuestHome;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class MakeReservationHomeRequestDto extends MakeReservationRequestDto {
    private String phoneNumber;

    public Guest makeGuest() {
        return GuestHome.builder()
                .name(super.guestName)
                .phoneNumber(this.phoneNumber)
                .build();
    }

    public MakeReservationHomeRequestDto(String dateRoomId, String guestName, String phoneNumber) {
        super(dateRoomId, guestName);
        this.phoneNumber = phoneNumber;
    }
}
