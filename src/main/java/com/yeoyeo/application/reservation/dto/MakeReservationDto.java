package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest;
import com.yeoyeo.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeReservationDto {
    private DateRoom dateRoom;
    protected Guest guest;
    private Payment payment;

    public MakeReservationDto(DateRoom dateRoom, Guest guest, Payment payment) {
        this.dateRoom = dateRoom;
        this.guest = guest;
        this.payment = payment;
    }
}
