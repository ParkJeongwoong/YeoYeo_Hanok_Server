package com.yeoyeo.application.reservation.dto.MakeReservationDto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.GuestBooking;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MakeReservationBookingDto extends MakeReservationDto {

    public MakeReservationBookingDto(List<DateRoom> dateRoomList, GuestBooking guest, int management_level) {
        super(dateRoomList, guest, management_level);
    }

}
