package com.yeoyeo.domain;

import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Reservation extends BaseTimeEntity {
    @Id
    private long id;

    @OneToOne
    @JoinColumn(name = "dateRoom_id", nullable = false)
    private DateRoom dateRoom;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private long reservationState; // 0 : 미결제, 1 : 숙박 예정, 2 : 숙박 완료 , -1 : 예약 취소, -2 : 환불 완료

    @Column
    private String reservedFrom;

    @Builder
    public Reservation(DateRoom dateRoom, Guest guest, Payment payment) {
        this.id = System.currentTimeMillis();
        this.dateRoom = dateRoom;
        this.guest = guest;
        this.reservedFrom = guest.getClass().getSimpleName();
        this.reservationState = 0;
        this.payment = payment;
        payment.setReservation(this);
    }

    public void setStatePaid() throws ReservationException {
        if (this.reservationState == 0) {
            this.reservationState = 1;
        } else {
            throw new ReservationException("미결제 상태가 아닙니다.");
        }
    }

    public void setStateCanceled() throws ReservationException {
        if (this.reservationState == 1) {
            this.reservationState = -1;
        } else {
            throw new ReservationException("결제가 완료된 예약이 아닙니다.");
        }
    }

    public void setStateRefund() throws ReservationException {
        if (this.reservationState == -1) {
            this.reservationState = -2;
        } else {
            throw new ReservationException("환불이 예정된 예약이 아닙니다.");
        }
    }
}
