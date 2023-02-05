package com.yeoyeo.domain;

import com.yeoyeo.application.room.dto.MakeRoomDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private long priceWeekend;

    @Column(nullable = false)
    private long priceHoliday;

    @Column(nullable = false)
    private long priceSpecial;

    @Builder
    public Room(String name) {
        this.name = name;
        this.price = 277777; // TEST 용
//        this.price = 250000;
        this.priceWeekend = 300000;
        this.priceHoliday = 330000;
        this.priceSpecial = 220000;
    }

    public Room(String name, long price, long priceWeekend, long priceHoliday, long priceSpecial) {
        this.name = name;
        this.price = price;
        this.priceWeekend = priceWeekend;
        this.priceHoliday = priceHoliday;
        this.priceSpecial = priceSpecial;
    }

    public Room(MakeRoomDto requestDto) {
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
        this.priceWeekend = requestDto.getPriceWeekend();
        this.priceHoliday = requestDto.getPriceHoliday();
        this.priceSpecial = requestDto.getPriceSpecial();
    }
}
