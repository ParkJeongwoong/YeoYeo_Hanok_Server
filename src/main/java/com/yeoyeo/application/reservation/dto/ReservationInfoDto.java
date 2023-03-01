package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.Reservation;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ReservationInfoDto {
    // 요약 정보
    private final long reservationId;
    // 방
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String roomName;
    private final long reservationState;
    // 손님
    private final String guestName;
    private final int guestCount;
    // 결제
    private final String paymentStatus;

    public ReservationInfoDto(Reservation entity) {
        this.reservationId = entity.getId();
        this.startDate = entity.getFirstDateRoom().getDate();
        this.endDate = entity.getLastDateRoom().getDate().plusDays(1);
        this.roomName = entity.getFirstDateRoom().getRoom().getName();
        this.reservationState = entity.getReservationState();
        this.guestName = entity.getGuest().getName();
        this.guestCount = entity.getGuest().getGuestCount();
        this.paymentStatus = entity.getPayment().getStatus();
    }
}
