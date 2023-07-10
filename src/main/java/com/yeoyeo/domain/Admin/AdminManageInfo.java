package com.yeoyeo.domain.Admin;

import com.yeoyeo.application.admin.dto.AdminManageInfoResponseDto;
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
    private long id;

//    @Enumerated(EnumType.STRING) // Enum은 DB와 연결했을 때 단점이 생기기 때문에 사용 X
    @Column(nullable = false)
//    private GuestType guestType;
    private int guestType; // 0: 홈페이지 예약 손님 , 1: 에어비앤비 예약 손님, 2: 전화 예약 손님, 3: 부킹닷컴 예약 손님

    @Column(nullable = false)
    private LocalDate checkin;

    @Column(nullable = false)
    private LocalDate checkout;

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

    @Column(nullable = false)
    private boolean activated;

    @Builder
    public AdminManageInfo(int guestType, LocalDate checkin, LocalDate checkout, Room room, String name, String phoneNumber, String request, int guestCount, Reservation reservation) {
        this.guestType = guestType;
        this.checkin = checkin;
        this.checkout = checkout;
        this.room = room;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.guestCount = guestCount;
        this.request = request;
        this.reservation = reservation;
        this.activated = true;
    }

    public AdminManageInfo(Reservation reservation) {
        switch (reservation.getReservedFrom()) {
            case "GuestHome":
                this.guestType = 0;
                this.checkin = reservation.getFirstDate();
                this.checkout = reservation.getLastDateRoom().getDate().plusDays(1);
                break;
            case "GuestAirbnb":
                if (reservation.getManagementLevel()>0) {
                    this.guestType = 2;
                    this.checkin = reservation.getFirstDate();
                    this.checkout = reservation.getFirstDate().plusDays(1);
                }
                else {
                    this.guestType = 1;
                    this.checkin = reservation.getFirstDate();
                    this.checkout = reservation.getLastDateRoom().getDate().plusDays(1);
                }
                break;
            case "GuestBooking":
                if (reservation.getManagementLevel()>0) {
                    this.guestType = 2;
                    this.checkin = reservation.getFirstDate();
                    this.checkout = reservation.getFirstDate().plusDays(1);
                }
                else {
                    this.guestType = 3;
                    this.checkin = reservation.getFirstDate();
                    this.checkout = reservation.getLastDateRoom().getDate().plusDays(1);
                }
                break;
            default:
                this.guestType = -1;
                this.checkin = reservation.getFirstDate();
                this.checkout = reservation.getLastDateRoom().getDate().plusDays(1);
        }
        this.room = reservation.getRoom();
        this.name = reservation.getGuest().getName();
        this.phoneNumber = reservation.getGuest().getPhoneNumber();
        this.guestCount = reservation.getGuest().getGuestCount();
        this.request = reservation.getGuest().getRequest();
        this.reservation = reservation;
        this.activated = true;
    }

    public AdminManageInfoResponseDto makeAdminManageInfoResponseDto() {
        return AdminManageInfoResponseDto.builder()
                .room(this.room)
                .checkIn(this.checkin)
                .checkOut(this.checkout)
                .guestType(this.guestType)
                .guestName(this.name)
                .guestPhoneNumber(this.phoneNumber)
                .guestCount(this.guestCount)
                .request(this.request)
                .reservation(this.reservation)
                .build();
    }

    public String getNumberOnlyPhoneNumber() {
        return this.phoneNumber.replaceAll("[^0-9]","");
    }

}
