package com.yeoyeo.domain;

import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Entity
public class Reservation extends BaseTimeEntity {

    @Id
    private long id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dateRoom_id")
    private List<DateRoom> dateRoomList;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column
    private String reservedFrom;

    @Column(nullable = false)
    private long reservationState; // 0 : 미결제, 1 : 숙박 예정, 2 : 숙박 완료 , -1 : 예약 취소, -2 : 환불 완료

//    @Column(nullable = false)
//    private String uniqueId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Builder
    public Reservation(List<DateRoom> dateRoomList, Guest guest) {
        this.id = System.currentTimeMillis();
        this.dateRoomList = dateRoomList;
        this.guest = guest;
        this.reservedFrom = guest.getClass().getSimpleName();
        this.reservationState = 0;
//        this.uniqueId = UUID.randomUUID().toString();
    }

    public void setPayment(Payment payment) throws ReservationException {
        this.payment = payment;
        setStatePaid();
    }

    public void setStatePaid() throws ReservationException {
        if (this.reservationState == 0) {
            this.reservationState = 1;
        } else {
            throw new ReservationException("미결제 상태가 아닙니다.");
        }
    }

    public void setStateComplete() throws ReservationException {
        if (this.reservationState == 1) {
            this.reservationState = 2;
        } else {
            throw new ReservationException("정상적인 예약이 아닙니다.");
        }
    }

    public void setStateCanceled() throws ReservationException {
        if (this.reservationState == 1 || this.reservationState == 0) {
            this.reservationState = -1;
        } else {
            throw new ReservationException("완료된 예약이 아닙니다.");
        }
    }

    public void setStateRefund() throws ReservationException {
        if (this.reservationState == -1) {
            this.reservationState = -2;
        } else {
            throw new ReservationException("환불이 예정된 예약이 아닙니다.");
        }
    }

    public DateRoom getFirstDateRoom() {
        if (this.dateRoomList.size()==0) return null;
        return this.dateRoomList.stream().sorted(Comparator.comparing(DateRoom::getDate)).collect(Collectors.toList()).get(0);
    }

    public DateRoom getLastDateRoom() {
        if (this.dateRoomList.size()==0) return null;
        return this.dateRoomList.stream().sorted(Comparator.comparing(DateRoom::getDate)).collect(Collectors.toList()).get(this.dateRoomList.size()-1);
    }

    public LocalDate getFirstDate() {
        if (getFirstDateRoom()==null) return null;
        return getFirstDateRoom().getDate();
    }

    public int getTotalPrice() {
        int totalPrice = 0;
        for (DateRoom dateRoom:this.dateRoomList) {
            totalPrice += dateRoom.getPrice();
        }
        return totalPrice;
    }

}
