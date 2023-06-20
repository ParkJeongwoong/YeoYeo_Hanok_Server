package com.yeoyeo.application.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangeRoomDefaultPriceRequestDto {

    private int price;
    private int priceWeekend;
    private int priceWeekdaySpecial;
    private int priceWeekendSpecial;

    public ChangeRoomDefaultPriceRequestDto(int price, int priceWeekend, int priceWeekdaySpecial, int priceWeekendSpecial) {
        this.price = price;
        this.priceWeekend = priceWeekend;
        this.priceWeekdaySpecial = priceWeekdaySpecial;
        this.priceWeekendSpecial = priceWeekendSpecial;
    }

}
