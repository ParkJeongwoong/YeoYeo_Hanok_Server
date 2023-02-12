package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.dateroom.dto.*;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("dateroom")
public class DateRoomController {

    private final DateRoomService dateRoomService;

    @ApiOperation(value = "Show all room", notes = "생성된 방-날짜 예약 정보 전체 조회")
    @GetMapping("show-all")
    public ResponseEntity<List<DateRoomInfoDto>> showAllDateRooms() {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.showAllDateRooms());
    }

    @ApiOperation(value = "Get Reservation Status", notes = "2달 치 예약 정보 조회")
    @GetMapping("/{year}/{month}")
    public ResponseEntity<List<DateRoomInfoDto>> show2MonthsDateRooms(@PathVariable("year") int year, @PathVariable("month") int month) {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.show2MonthsDateRooms(year, month));
    }

}
