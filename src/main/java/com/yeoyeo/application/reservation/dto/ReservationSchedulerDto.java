package com.yeoyeo.application.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationSchedulerDto {
    private final long reservationId;
    private final LocalDateTime expirationTime;

    @Builder
    public ReservationSchedulerDto(long reservationId, LocalDateTime expirationTime) {
        this.reservationId = reservationId;
        this.expirationTime = expirationTime;
    }
}
