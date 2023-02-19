package com.yeoyeo.application.dateroom.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateRoomInfoDto {
    private final String merchant_uid;
    private final LocalDate date;
    private final long roomId;
    private final String roomName;
    private final int price;
    private final long reservationState;

    public DateRoomInfoDto(DateRoom entity) {
        Room room = entity.getRoom();

        this.merchant_uid = entity.getDateRoomId()+entity.getReservationCount()+"test"; // Todo - Test용 상품번호 지우기
        this.date = entity.getDate();
        this.roomId = room.getId();
        this.roomName = room.getName();
        this.price = entity.getPrice();
        this.reservationState = entity.getRoomReservationState();
    }
}
