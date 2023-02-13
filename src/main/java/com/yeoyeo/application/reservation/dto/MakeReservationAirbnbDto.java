package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestAirbnb;
import com.yeoyeo.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeReservationAirbnbDto extends MakeReservationDto {

    public MakeReservationAirbnbDto(DateRoom dateRoom, GuestAirbnb guest, Payment payment) {
        super(dateRoom, guest, payment);
    }

}
