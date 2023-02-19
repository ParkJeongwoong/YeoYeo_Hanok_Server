package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeRequestDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.service.ReservationService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final DateRoomRepository dateRoomRepository;

    @ApiOperation(value = "Reservation", notes = "(관리자용) 관리자의 해당 날짜 예약 처리")
    @Transactional
    @PostMapping("/home")
    public ResponseEntity<GeneralResponseDto> createReservation(@RequestBody MakeReservationHomeRequestDto requestDto) {
        try {
            reservationService.makeReservation(requestDto.getMakeReservationHomeDto(dateRoomRepository));
            return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).message("예약이 확정되었습니다.").build());
        } catch (ReservationException e) {
            log.error("createReservation 에러", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message("예약 중 오류가 발생했습니다.").build());
        }
    }

    @ApiOperation(value = "Reservation", notes = "(관리자용) 관리자의 해당 날짜 예약 취소 처리")
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<GeneralResponseDto> cancelReservation(@PathVariable("reservationId") long reservationId) {
        GeneralResponseDto responseDto = reservationService.cancel(reservationId);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
