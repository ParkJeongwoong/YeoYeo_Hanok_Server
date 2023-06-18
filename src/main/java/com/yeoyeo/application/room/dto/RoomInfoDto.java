package com.yeoyeo.application.room.dto;

import com.yeoyeo.domain.Room;
import lombok.Getter;

@Getter
public class RoomInfoDto {
    private final long roomId;
    private final String roomName;
    private final int price;
    private final int priceWeekend;
    private final int priceWeekdaySpecial;
    private final int priceWeekendSpecial;

    public RoomInfoDto(Room entity) {
        this.roomId = entity.getId();
        this.roomName = entity.getName();
        this.price = entity.getPrice();
        this.priceWeekend = entity.getPriceWeekend();
        this.priceWeekdaySpecial = entity.getPriceWeekdaySpecial();
        this.priceWeekendSpecial = entity.getPriceWeekendSpecial();
    }
}
