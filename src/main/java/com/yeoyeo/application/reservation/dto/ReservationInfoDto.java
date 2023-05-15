package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ReservationInfoDto {
    // 요약 정보
    private final long reservationId;
    // 방
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final String roomName;
    private final long reservationState;
    // 손님
    private final String guestName;
    private final int guestCount;
    private final int managementLevel;
    // 결제
    private final String paymentStatus;

    public ReservationInfoDto(Reservation entity) {
        DateRoom firstDateRoom = entity.getFirstDateRoom();
        DateRoom lastDateRoom = entity.getLastDateRoom();
        Payment payment = entity.getPayment();

        this.reservationId = entity.getId();
        if (firstDateRoom!=null) this.checkInDate = firstDateRoom.getDate(); else this.checkInDate = null;
        if (lastDateRoom!=null) this.checkOutDate = lastDateRoom.getDate().plusDays(1); else this.checkOutDate = null;
        if (firstDateRoom!=null) this.roomName = firstDateRoom.getRoom().getName(); else this.roomName = null;
        this.reservationState = entity.getReservationState();
        this.guestName = entity.getGuest().getName();
        this.guestCount = entity.getGuest().getGuestCount();
        this.managementLevel = entity.getManagementLevel();
        if (payment!=null) this.paymentStatus = entity.getPayment().getStatus(); else this.paymentStatus = "unPaid";
    }
}
