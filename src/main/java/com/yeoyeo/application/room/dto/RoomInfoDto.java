package com.yeoyeo.application.room.dto;

import com.yeoyeo.domain.Room;
import lombok.Getter;

@Getter
public class RoomInfoDto {
    private final long roomId;
    private final String roomName;

    public RoomInfoDto(Room entity) {
        this.roomId = entity.getId();
        this.roomName = entity.getName();
    }
}
