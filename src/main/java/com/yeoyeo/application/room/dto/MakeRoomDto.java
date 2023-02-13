package com.yeoyeo.application.room.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeRoomDto {
    String name;
    int price;
    int priceWeekend;
    int priceHoliday;
    int priceSpecial;

    @Builder
    public MakeRoomDto(String name, int price, int priceWeekend, int priceHoliday, int priceSpecial) {
        this.name = name;
        this.price = price;
        this.priceWeekend = priceWeekend;
        this.priceHoliday = priceHoliday;
        this.priceSpecial = priceSpecial;
    }
}
