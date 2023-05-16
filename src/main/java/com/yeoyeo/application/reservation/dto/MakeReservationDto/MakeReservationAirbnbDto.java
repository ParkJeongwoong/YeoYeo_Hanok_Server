package com.yeoyeo.application.reservation.dto.MakeReservationDto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.GuestAirbnb;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MakeReservationAirbnbDto extends MakeReservationDto {

    public MakeReservationAirbnbDto(List<DateRoom> dateRoomList, GuestAirbnb guest, int management_level) {
        super(dateRoomList, guest, management_level);
    }

}
