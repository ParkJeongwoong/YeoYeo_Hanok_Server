package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestHome;
import com.yeoyeo.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class MakeReservationHomeDto extends MakeReservationDto {

    public MakeReservationHomeDto(DateRoom dateRoom, GuestHome guest, Payment payment) {
        super(dateRoom, guest, payment);
    }

}
