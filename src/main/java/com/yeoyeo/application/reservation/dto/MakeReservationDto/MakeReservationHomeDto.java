package com.yeoyeo.application.reservation.dto.MakeReservationDto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.GuestHome;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
public class MakeReservationHomeDto extends MakeReservationDto {

    public MakeReservationHomeDto(List<DateRoom> dateRoomList, GuestHome guest) {
        super(dateRoomList, guest, 1);
    }

}
