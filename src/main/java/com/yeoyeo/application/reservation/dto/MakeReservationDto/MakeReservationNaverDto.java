package com.yeoyeo.application.reservation.dto.MakeReservationDto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.GuestNaver;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class MakeReservationNaverDto extends MakeReservationDto {

    public MakeReservationNaverDto(List<DateRoom> dateRoomList, GuestNaver guest, int management_level) {
        super(dateRoomList, guest, 1);
    }

}
