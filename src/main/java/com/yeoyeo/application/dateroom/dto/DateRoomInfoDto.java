package com.yeoyeo.application.dateroom.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import lombok.Getter;

@Getter
public class DateRoomInfoDto {
    private final String dateRoomId;
    private final long roomId;
    private final String roomName;
    private final int price;
    private final int priceType;
    private final long reservationState;
    private final boolean isReservable;

    public DateRoomInfoDto(DateRoom entity) {
        Room room = entity.getRoom();
        this.dateRoomId = entity.getId();
        this.roomId = room.getId();
        this.roomName = room.getName();
        this.price = entity.getPrice();
        this.priceType = entity.getPriceType();
        this.reservationState = entity.getRoomReservationState();
        this.isReservable = entity.isReservable();
    }
}
