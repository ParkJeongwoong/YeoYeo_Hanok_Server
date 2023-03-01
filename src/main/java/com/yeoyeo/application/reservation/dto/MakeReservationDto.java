package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest;
import com.yeoyeo.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MakeReservationDto {
    private List<DateRoom> dateRoomList;
    protected Guest guest;

    public MakeReservationDto(List<DateRoom> dateRoomList, Guest guest) {
        this.dateRoomList = dateRoomList;
        this.guest = guest;
    }
}
