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

//    @Enumerated(EnumType.STRING) // Enum은 DB와 연결했을 때 단점이 생기기 때문에 사용 X
    @Column(nullable = false)
//    private GuestType guestType;
    private int guestType; // 0: 홈페이지 예약 손님 , 1: 에어비앤비 예약 손님, 2: 문의 예약 손님

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(length = 30)
    private String name;

    @Column(length = 20)
    private String phoneNumber;

    @Column
    private int guestCount;

    @Column(length = 255)
    private String request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Builder
    public AdminManageInfo(int guestType, LocalDate checkIn, LocalDate checkOut, Room room, String name, String phoneNumber, int guestCount, Reservation reservation) {
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
                this.guestType = 0;
                break;
            case "GuestAirbnb":
                if (reservation.getManagementLevel()>0) this.guestType =2;
                else this.guestType = 1;
                break;
            default:
                this.guestType = -1;
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
