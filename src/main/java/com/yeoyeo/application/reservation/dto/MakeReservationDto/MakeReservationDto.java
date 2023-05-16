package com.yeoyeo.application.reservation.dto.MakeReservationDto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.Guest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MakeReservationDto {
    private List<DateRoom> dateRoomList;
    protected Guest guest;
    private int management_level;

    public MakeReservationDto(List<DateRoom> dateRoomList, Guest guest, int management_level) {
        this.dateRoomList = dateRoomList;
        this.guest = guest;
        this.management_level = management_level;
    }
}
