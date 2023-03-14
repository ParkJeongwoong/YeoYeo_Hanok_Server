package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ChangeDateRoomListStatusRequestDto {

    private final List<String> dateRoomIdList;
    private final long roomReservationState;

    public ChangeDateRoomListStatusRequestDto(List<String> dateRoomIdList, long roomReservationState) {
        this.dateRoomIdList = dateRoomIdList;
        this.roomReservationState = roomReservationState;
    }

}
