package com.yeoyeo.application.admin.dto;

import lombok.Getter;

@Getter
public class ChangeRoomDefaultPriceRequestDto {

    private final int price;
    private final int priceWeekend;
    private final int priceWeekdaySpecial;
    private final int priceWeekendSpecial;

    public ChangeRoomDefaultPriceRequestDto(int price, int priceWeekend, int priceWeekdaySpecial, int priceWeekendSpecial) {
        this.price = price;
        this.priceWeekend = priceWeekend;
        this.priceWeekdaySpecial = priceWeekdaySpecial;
        this.priceWeekendSpecial = priceWeekendSpecial;
    }

}
