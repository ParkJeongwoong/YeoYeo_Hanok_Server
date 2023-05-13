package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChangeDateRoomListPriceRequestDto {

    private List<String> dateRoomIdList;
    private int priceType;
    private int price;

    public ChangeDateRoomListPriceRequestDto(List<String> dateRoomIdList, int priceType, int price) {
        this.dateRoomIdList = dateRoomIdList;
        this.priceType = priceType;
        this.price = price;
    }

}
