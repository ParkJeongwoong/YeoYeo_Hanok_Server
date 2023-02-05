package com.yeoyeo.domain;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
public class DateRoom {
    @Id
    private String dateRoomId;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private int priceType; // 0 : 평일, 1 : 주말, 2 : 연휴, 3 : 특별가

    @Column(nullable = false)
    private long roomReservationState; // 0 : 예약 가능, 1 : 예약 완료

    @Builder
    DateRoom(LocalDate date, Room room) {
        this.date = date;
        this.room = room;
        this.roomReservationState = 0;
        this.dateRoomId = date.toString() + "&&" + room.getId();
        setDefaultPriceType();
        setPrice();
    }

    public void setStateBooked() throws RoomReservationException {
        if (this.roomReservationState == 0) {
            this.roomReservationState = 1;
        } else {
            throw new RoomReservationException("예약이 불가능한 날짜입니다.");
        }
    }

    public void resetState() {
        if (this.roomReservationState == 1) {
            this.roomReservationState = 0;
        } else {
            throw new RoomReservationException("예약된 날짜가 아닙니다.");
        }
    }

    public long changePriceType(int priceType) {
        this.priceType = priceType;
        setPrice();
        return this.priceType;
    }

    private void setDefaultPriceType() {
        DayOfWeek dayOfWeek = this.date.getDayOfWeek();
        switch (dayOfWeek) {
            case FRIDAY:
            case SATURDAY:
                this.priceType = 1;
                break;
            default:
                this.priceType = 0;
                break;
        }
    }

    private void setPrice() {
        switch (this.priceType) {
            case 0:
                this.price = this.room.getPrice();
                break;
            case 1:
                this.price = this.room.getPriceWeekend();
                break;
            case 2:
                this.price = this.room.getPriceHoliday();
                break;
            case 3:
                this.price = this.room.getPriceSpecial();
                break;
        }
    }
}
