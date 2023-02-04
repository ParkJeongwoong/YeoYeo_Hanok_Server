package com.yeoyeo.domain;

import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Reservation extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "dateRoom_id", nullable = false)
    private DateRoom dateRoom;

    @OneToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column(nullable = false)
    private long reservationState; // 0 : 결제 대기, 1 : 결제 완료, -1 : 결제 시간 초과, -2 : 환불

    private String reservedFrom;

    @Builder
    public Reservation(DateRoom dateRoom, Guest guest) {
        this.dateRoom = dateRoom;
        this.guest = guest;
        this.reservedFrom = guest.getClass().getSimpleName();
        this.reservationState = 0;
    }

    public void setStatePayed() throws ReservationException {
        if (this.reservationState == 0) {
            this.reservationState = 1;
        } else {
            throw new ReservationException("결제가 불가능한 예약입니다.");
        }
    }

    public void setStateTimeOut() throws ReservationException {
        if (this.reservationState == 0) {
            this.reservationState = -1;
        } else {
            throw new ReservationException("결제 대기 예약이 아닙니다.");
        }
    }

    public void setStateRefund() throws ReservationException {
        if (this.reservationState == 1) {
            this.reservationState = -2;
        } else {
            throw new ReservationException("결제가 완료된 예약이 아닙니다.");
        }
    }
}
