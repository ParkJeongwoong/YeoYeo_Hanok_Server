package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.Guest;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeReservationRequestDto {
    private String dateRoomId;
    protected String guestName;

    public Guest makeGuest() {
        return new Guest(guestName);
    }

    public MakeReservationRequestDto(String dateRoomId, String guestName) {
        this.dateRoomId = dateRoomId;
        this.guestName = guestName;
    }
}
