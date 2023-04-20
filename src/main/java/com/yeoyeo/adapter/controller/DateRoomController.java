package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
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
    public ResponseEntity<List<DateRoomInfoByDateDto>> showAllDateRooms() {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.showAllDateRooms());
    }

    @ApiOperation(value = "Get Reservation Status", notes = "2달 치 예약 정보 조회")
    @GetMapping("/{year}/{month}")
    public ResponseEntity<DateRoom2MonthDto> show2MonthsDateRooms(@PathVariable("year") int year, @PathVariable("month") int month) {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.show2MonthsDateRooms(year, month));
    }

    @ApiOperation(value = "Get Total Price", notes = "8자리 시작일, 종료일을 바탕으로 비용 계산 (인원 수에 따른 추가금 미포함)")
    @GetMapping("/price/{roomId}/{checkInDate}/{checkOutDate}")
    public ResponseEntity<DateRoomPriceInfoDto> getTotalPrice(@PathVariable("roomId") long roomId, @PathVariable("checkInDate") String checkInDate, @PathVariable("checkOutDate") String checkOutDate) {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.getTotalPrice(roomId, checkInDate, checkOutDate));
    }

    private final CalendarService calendarService;
    @GetMapping("/test")
    public void test() {
        calendarService.readAirbnbICSFile();
    }
    @GetMapping("/test2")
    public void test2() {
        calendarService.getAirbnbICSFile();
    }
    @GetMapping("/test3")
    public void test3() {
        calendarService.writeICSFile();
    }
    @GetMapping("/test/airbnb/a")
    public void testAirbnbA() { calendarService.syncInICSFile_Airbnb_A(); }
    @GetMapping("/test/airbnb/b")
    public void testAirbnbB() { calendarService.syncInICSFile_Airbnb_B(); }


}
