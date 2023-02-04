package com.yeoyeo.application.room.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeRoomDto {
    String name;
    long price;

    @Builder
    public MakeRoomDto(String name, long price) {
        this.name = name;
        this.price = price;
    }
}
