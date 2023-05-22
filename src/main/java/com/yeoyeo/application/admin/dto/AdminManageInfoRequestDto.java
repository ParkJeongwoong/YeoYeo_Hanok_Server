package com.yeoyeo.application.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
public class AdminManageInfoRequestDto {

    private LocalDate checkIn;
    private long roomId;

    private LocalDate checkOut;
    private String guestName;
    private String guestPhoneNumber;
    private int guestCount;
    private String request;

    private long reservationId;

    @Builder
    public AdminManageInfoRequestDto(LocalDate checkIn, long roomId, LocalDate checkOut, String guestName, String guestPhoneNumber, int guestCount, String request, long reservationId) {
        this.checkIn = checkIn;
        this.roomId = roomId;
        this.checkOut = checkOut;
        this.guestName = guestName;
        this.guestPhoneNumber = guestPhoneNumber;
        this.guestCount = guestCount;
        this.request = request;
        this.reservationId = reservationId;
    }

}