package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ChangeDateRoomListPriceRequestDto {

    private final List<String> dateRoomIdList;
    private final int priceType;
    private final int price;

    public ChangeDateRoomListPriceRequestDto(List<String> dateRoomIdList, int priceType, int price) {
        this.dateRoomIdList = dateRoomIdList;
        this.priceType = priceType;
        this.price = price;
    }

}
