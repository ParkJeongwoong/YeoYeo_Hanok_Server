package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;

@Getter
public class DateRoomPriceInfoDto {

    private final int totalPrice;
    private final int originalPrice;
    private final int period;
    private final int discountedPrice;

    public DateRoomPriceInfoDto(int totalPrice, int originalPrice, int discountedPrice, int period) {
        this.totalPrice = totalPrice;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.period = period;
    }

}
