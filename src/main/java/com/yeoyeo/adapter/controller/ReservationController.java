package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("reservation")
public class ReservationController {

    private final ReservationService reservationService;

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<GeneralResponseDto> cancelReservation(@PathVariable("reservationId") long reservationId) {
        GeneralResponseDto responseDto = reservationService.cancel(reservationId);
        if (responseDto.getSuccessYN().equals("N")) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
