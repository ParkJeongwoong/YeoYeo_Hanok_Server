package com.yeoyeo.application.admin.dto;

import com.yeoyeo.domain.Admin.GuestType;
import com.yeoyeo.domain.Room;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AdminManageInfoResponseDto {

    private final String roomName;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final GuestType guestType;
    private final String guestName;
    private final String guestPhoneNumber;
    private final int guestCount;
    private final String request;

    @Builder
    public AdminManageInfoResponseDto(Room room, LocalDate checkIn, LocalDate checkOut, int guestType, String guestName, String guestPhoneNumber, int guestCount, String request) {
        this.roomName = room.getName();
        this.checkIn = checkIn;
        this.checkOut = checkOut;
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
            default:
                this.guestType = GuestType.OTHER;
        }
        this.guestName = guestName;
        this.guestPhoneNumber = guestPhoneNumber;
        this.guestCount = guestCount;
        this.request = request;
    }

}
