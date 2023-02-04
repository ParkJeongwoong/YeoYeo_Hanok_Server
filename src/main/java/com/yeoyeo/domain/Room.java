package com.yeoyeo.domain;

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
    public Room(String name, long price, long priceWeekend, long priceHoliday, long priceSpecial) {
        this.name = name;
        this.price = price;
        this.priceWeekend = priceWeekend;
        this.priceHoliday = priceHoliday;
        this.priceSpecial = priceSpecial;
    }
}
