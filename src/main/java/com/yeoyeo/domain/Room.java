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
    private int price;

    @Column(nullable = false)
    private int priceWeekend;

    @Column(nullable = false)
    private int priceHoliday;

    @Column(nullable = false)
    private int priceSpecial;

    @Builder
    public Room(String name) {
        this.name = name;
        this.price = 250000;
        this.priceWeekend = 300000;
        this.priceHoliday = 330000;
        this.priceSpecial = 220000;
    }

    public Room(String name, int price, int priceWeekend, int priceHoliday, int priceSpecial) {
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
