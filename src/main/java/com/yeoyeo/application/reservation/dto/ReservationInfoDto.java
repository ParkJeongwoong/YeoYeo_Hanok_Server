package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ReservationInfoDto {
    private final LocalDate date;

    @Builder
    public ReservationInfoDto(Reservation entity) {
        this.date = entity.getDateRoom().getDate();
    }
}
