package com.yeoyeo.application.admin.dto;

import com.yeoyeo.domain.Admin.GuestType;
import com.yeoyeo.domain.Reservation;
import com.yeoyeo.domain.Room;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AdminManageInfoResponseDto {

    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final String roomName;
    private final GuestType guestType;
    private final String guestName;
    private final String guestPhoneNumber;
    private final int guestCount;
    private final String request;
    private final long reservationId;
    private final LocalDate reservationCheckOut;

    @Builder
    public AdminManageInfoResponseDto(Room room, LocalDate checkIn, LocalDate checkOut, int guestType, String guestName, String guestPhoneNumber, int guestCount, String request, Reservation reservation) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomName = room.getName();
        switch (guestType) {
            case 0:
                this.guestType = GuestType.HOME;
                break;
            case 1:
                this.guestType = GuestType.AIRBNB;
                break;
            case 2:
                this.guestType = GuestType.DIRECT;
                break;
            case 3:
                this.guestType = GuestType.BOOKING;
                break;
            default:
                this.guestType = GuestType.OTHER;
        }
        this.guestName = guestName;
        this.guestPhoneNumber = guestPhoneNumber;
        this.guestCount = guestCount;
        this.request = request;
        if (reservation == null) {
            this.reservationId = -1;
            this.reservationCheckOut = this.checkOut;
        } else {
            this.reservationId = reservation.getId();
            this.reservationCheckOut = reservation.getLastDateRoom().getDate().plusDays(1);
        }
    }

}
