package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.room.dto.RoomInfoDto;
import com.yeoyeo.application.room.service.RoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"객실 정보 API"})
@RequiredArgsConstructor
@RestController
@RequestMapping("room")
public class RoomController {

    private final RoomService roomService;

    @ApiOperation(value = "Show rooms", notes = "방 정보 조회")
    @GetMapping("show-all")
    public ResponseEntity<List<RoomInfoDto>> showAllRooms() {
        return ResponseEntity.status(HttpStatus.OK).body(roomService.showAllRooms());
    }
}
