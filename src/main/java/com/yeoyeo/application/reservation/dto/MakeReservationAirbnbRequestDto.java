package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.Guest;
import com.yeoyeo.domain.GuestAirbnb;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeReservationAirbnbRequestDto extends MakeReservationRequestDto {

    public Guest makeGuest() {
        return GuestAirbnb.builder().name(guestName).build();
    }

    public MakeReservationAirbnbRequestDto(String dateRoomId, String guestName) {
        super(dateRoomId, guestName);
    }

}
