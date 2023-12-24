package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.room.dto.RoomInfoDto;
import com.yeoyeo.application.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "객실 정보 API", description = "객실 정보 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("room")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "방 정보 조회", description = "전체 방 정보 조회")
    @GetMapping("show-all")
    public ResponseEntity<List<RoomInfoDto>> showAllRooms() {
        return ResponseEntity.status(HttpStatus.OK).body(roomService.showAllRooms());
    }
}
