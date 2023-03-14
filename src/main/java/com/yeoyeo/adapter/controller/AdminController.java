package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.admin.dto.ChangeRoomDefaultPriceRequestDto;
import com.yeoyeo.application.admin.dto.SignupDto;
import com.yeoyeo.application.admin.service.AuthService;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.dto.ChangeDateRoomListPriceRequestDto;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import com.yeoyeo.application.room.service.RoomService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("admin")
public class AdminController {

    private final AuthService authService;
    private final RoomService roomService;
    private final DateRoomService dateRoomService;

    @PostMapping("/signup")
    public String test2(@RequestBody SignupDto requestDto) {
        return authService.signup(requestDto);
    }

    @ApiOperation(value = "Change Default Price", notes = "방의 기본가(평일가격, 주말가격, 성수기 평일가격, 성수기 주말가격) 설정")
    @PutMapping("/room/{roomId}")
    public ResponseEntity<GeneralResponseDto> changeRoomDefaultPrice(@PathVariable long roomId, @RequestBody ChangeRoomDefaultPriceRequestDto requestDto) {
        GeneralResponseDto responseDto = roomService.changeRoomDefaultPrice(roomId, requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Change (Multiple) Date Rooms Price", notes = "배열 형태의 dateRoomId를 모아 dateroom 가격 일괄 변경")
    @PutMapping("/dateroom/price")
    public ResponseEntity<GeneralResponseDto> changeDateRoomListPrice(@PathVariable long roomId, @RequestBody ChangeDateRoomListPriceRequestDto requestDto) {
        GeneralResponseDto responseDto = dateRoomService.changeDateRoomListPrice(requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
