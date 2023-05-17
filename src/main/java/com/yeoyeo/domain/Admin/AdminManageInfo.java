package com.yeoyeo.domain.Admin;

import com.yeoyeo.domain.Reservation;
import com.yeoyeo.domain.Room;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDate;

@Slf4j
@NoArgsConstructor
@Getter
@Setter
@Entity
public class AdminManageInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Enumerated(EnumType.STRING)
    @Column
    private GuestType guestType;

    @Column
    private LocalDate checkIn;

    @Column
    private LocalDate checkOut;

    @Column
    private Room room;

    @Column
    private String name;

    @Column
    private String phoneNumber;

    @Column
    private int guestCount;

    @Column
    private Reservation reservation;

    @Builder
    public AdminManageInfo(GuestType guestType, LocalDate checkIn, LocalDate checkOut, Room room, String name, String phoneNumber, int guestCount, Reservation reservation) {
        this.guestType = guestType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.room = room;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.guestCount = guestCount;
        this.reservation = reservation;
    }

    public AdminManageInfo(Reservation reservation) {
        switch (reservation.getReservedFrom()) {
            case "GuestHome":
                this.guestType = GuestType.HOME;
                break;
            case "GuestAirbnb":
                if (reservation.getManagementLevel()>0) this.guestType =GuestType.DIRECT;
                else this.guestType = GuestType.AIRBNB;
                break;
            default:
                this.guestType = GuestType.OTHER;
        }
        this.checkIn = reservation.getFirstDate();
        this.checkOut = reservation.getLastDateRoom().getDate();
        this.room = reservation.getRoom();
        this.name = reservation.getGuest().getName();
        this.phoneNumber = reservation.getGuest().getPhoneNumber();
        this.guestCount = reservation.getGuest().getGuestCount();
        this.reservation = reservation;
    }

    public void sendConfirmMsg() {

    }

    public void sendCheckInMsg() {

    }

    public void noticeAdmin() {

    }

}
