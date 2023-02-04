package com.yeoyeo.application.dateroom.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MakeDateRoomDto {
    private LocalDate date;
    private long roomId;

    @Builder
    public MakeDateRoomDto(LocalDate date, long roomId) {
        this.date = date;
        this.roomId = roomId;
    }
}
