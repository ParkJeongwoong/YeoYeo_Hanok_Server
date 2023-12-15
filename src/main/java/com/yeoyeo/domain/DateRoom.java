package com.yeoyeo.domain;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.HolidayRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.OptimisticLocking;

@Slf4j
@Getter
@NoArgsConstructor
@Entity
@Cacheable // 2차 캐시 활성화
@OptimisticLocking
public class DateRoom {
    @Id
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int priceType; // 0 : 직접 설정, 1 : 평일, 2 : 주말, 3 : 평일(특가), 4 : 주말(특가)

    @Column(nullable = false)
    private long roomReservationState; // 0 : 예약 가능, 1 : 예약 완료, 2 : 예약 대기

    @Column(nullable = false)
    private boolean isReservable;

    @OneToMany(mappedBy = "dateRoom")
    private final List<MapDateRoomReservation> mapDateRoomReservations = new ArrayList<>();

    @Version
    private int version;

    @Builder
    DateRoom(LocalDate date, Room room, HolidayRepository holidayRepository) {
        this.id = date.toString().replaceAll("[^0-9]","") + room.getId();
        this.date = date;
        this.room = room;
        this.roomReservationState = 0;
        setDefaultPriceType(holidayRepository);
        setPrice();
        this.isReservable = true;
    }

    public void setStateBooked() throws RoomReservationException {
        if (this.roomReservationState != 1) {
            this.roomReservationState = 1;
        } else {
            throw new RoomReservationException("예약이 불가능한 날짜입니다.");
        }
    }

    public void setStateWaiting() throws RoomReservationException {
        if (this.roomReservationState == 0) {
            this.roomReservationState = 2;
        } else {
            throw new RoomReservationException("예약 대기가 불가능한 날짜입니다.");
        }
    }

    // 사용하기 전 예약된 건이 없는지 확인하는 로직이 반드시 필요
    public void resetState() {
        this.roomReservationState = 0;
    }

    public void changePriceType(int priceType) {
        this.priceType = priceType;
        setPrice();
    }

    public void changePrice(int price) {
        this.price = price;
        this.priceType = 0;
    }

    public void resetDefaultPriceType(HolidayRepository holidayRepository) {
        if (this.priceType == 1 || this.priceType == 2) setDefaultPriceType(holidayRepository);
        if (this.priceType != 0) setPrice();
    }

    public void setPrice() {
        switch (this.priceType) {
            case 1:
                this.price = this.room.getPrice();
                break;
            case 2:
                this.price = this.room.getPriceWeekend();
                break;
            case 3:
                this.price = this.room.getPriceWeekdaySpecial();
                break;
            case 4:
                this.price = this.room.getPriceWeekendSpecial();
                break;
        }
    }

    private void setDefaultPriceType(HolidayRepository holidayRepository) {
        DayOfWeek dayOfWeek = this.date.getDayOfWeek();
        switch (dayOfWeek) {
            case FRIDAY:
            case SATURDAY:
                this.priceType = 2;
                break;
            default:
                this.priceType = 1;
                break;
        }
        if (checkHoliday(holidayRepository)) {
            this.priceType = 2;
        }
    }

    private boolean checkHoliday(HolidayRepository holidayRepository) {
        LocalDate dayAfter = this.date.plusDays(1);
        Holiday holiday = holidayRepository.findById(dayAfter).orElse(null);
        return holiday != null;
    }

    public void setReservable() {
        this.isReservable = true;
    }

    public void setUnReservable() {
        this.isReservable = false;
    }

}
