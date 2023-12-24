package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.dateroom.dto.DateRoom2MonthDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoByDateDto;
import com.yeoyeo.application.dateroom.dto.DateRoomPriceInfoDto;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "날짜-방 정보 API", description = "날짜-방 정보 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("dateroom")
public class DateRoomController {

    private final DateRoomService dateRoomService;

    @Operation(summary = "방-날짜 예약 정보 조회 (전체)", description = "생성된 방-날짜 예약 정보 전체 조회")
    @GetMapping("show-all")
    public ResponseEntity<List<DateRoomInfoByDateDto>> showAllDateRooms() {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.showAllDateRooms());
    }

    @Operation(summary = "방-날짜 예약 정보 조회 (2개월)", description = "2달치 방-날짜 예약 정보 조회")
    @GetMapping("/{year}/{month}")
    public ResponseEntity<DateRoom2MonthDto> show2MonthsDateRooms(@PathVariable("year") int year, @PathVariable("month") int month) {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.show2MonthsDateRooms(year, month));
    }

    @Operation(summary = "총 가격 계산", description = "8자리 시작일, 종료일을 바탕으로 비용 계산 (인원 수에 따른 추가금 미포함)")
    @GetMapping("/price/{roomId}/{checkInDate}/{checkOutDate}")
    public ResponseEntity<DateRoomPriceInfoDto> getTotalPrice(@PathVariable("roomId") long roomId, @PathVariable("checkInDate") String checkInDate, @PathVariable("checkOutDate") String checkOutDate) {
        return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.getTotalPrice(roomId, checkInDate, checkOutDate));
    }

}
