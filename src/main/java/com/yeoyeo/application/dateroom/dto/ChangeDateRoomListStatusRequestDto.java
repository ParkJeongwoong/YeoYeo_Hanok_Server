package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChangeDateRoomListStatusRequestDto {

    private List<String> dateRoomIdList;
    private long roomReservationState;

    public ChangeDateRoomListStatusRequestDto(List<String> dateRoomIdList, long roomReservationState) {
        this.dateRoomIdList = dateRoomIdList;
        this.roomReservationState = roomReservationState;
    }

}
