package com.yeoyeo.application.dateroom.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DateRoomInfoDto implements Serializable {
    private String dateRoomId;
    private long roomId;
    private String roomName;
    private int price;
    private int priceType;
    private long reservationState;
    private boolean reservable;

    public DateRoomInfoDto(DateRoom entity) {
        Room room = entity.getRoom();
        this.dateRoomId = entity.getId();
        this.roomId = room.getId();
        this.roomName = room.getName();
        this.price = entity.getPrice();
        this.priceType = entity.getPriceType();
        this.reservationState = entity.getRoomReservationState();
        this.reservable = entity.isReservable();
    }
}
