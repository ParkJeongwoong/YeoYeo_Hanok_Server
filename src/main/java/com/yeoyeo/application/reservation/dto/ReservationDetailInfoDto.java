package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.domain.Reservation;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ReservationDetailInfoDto {
    // 상세 정보
    // 방
    private final LocalDate date;
    private final String roomName;
    private final long reservationState;
    // 손님
    private final String guestName;
    private final String phoneNumber;
    private final String email;
    private final int guestCount;
    private final String request;
    private final String reservedFrom;
    // 결제
    private final int roomPrice;
    private final Integer paidAmount;
    private final Integer canceled_amount;
    private final String paymentStatus;

    public ReservationDetailInfoDto(Reservation entity) {
        this.date = entity.getDateRoom().getDate();
        this.roomName = entity.getDateRoom().getRoom().getName();
        this.reservationState = entity.getReservationState();
        this.guestName = entity.getGuest().getName();
        this.phoneNumber = entity.getGuest().getPhoneNumber();
        this.email = entity.getGuest().getEmail();
        this.guestCount = entity.getGuest().getGuestCount();
        this.request = entity.getGuest().getRequest();
        this.reservedFrom = entity.getReservedFrom();
        this.roomPrice = entity.getDateRoom().getPrice();
        this.paidAmount = entity.getPayment().getAmount();
        this.canceled_amount = entity.getPayment().getCanceled_amount();
        this.paymentStatus = entity.getPayment().getStatus();
    }
}
