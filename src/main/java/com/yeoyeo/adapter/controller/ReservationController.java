package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeRequestDto;
import com.yeoyeo.application.reservation.dto.ReservationDetailInfoDto;
import com.yeoyeo.application.reservation.dto.ReservationInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.application.sms.dto.SendMessageResponseDto;
import com.yeoyeo.application.sms.service.SmsService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final SmsService smsService;

    private final DateRoomRepository dateRoomRepository;

    @ApiOperation(value = "Reservation", notes = "예약 - 아임포트 결제 모듈로 호출 전 예약 정보 생성 => merchant_uid 응답")
    @Transactional
    @PostMapping("/reserve")
    public ResponseEntity<GeneralResponseDto> createReservation(@RequestBody MakeReservationHomeRequestDto requestDto) {
        try {
            long reservationId = reservationService.createReservation(requestDto.getMakeReservationHomeDto(dateRoomRepository));
            return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).resultId(reservationId).message("예약 정보 생성 완료").build());
        } catch (ReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message(e.getMessage()).build());
        }
    }

    @ApiOperation(value = "Reservation Detail", notes = "예약 상세 정보 조회")
    @GetMapping("/detail/{reservationId}/{phoneNumber}")
    public ResponseEntity<ReservationDetailInfoDto> getReservationInfo(@PathVariable("reservationId") long reservationId, @PathVariable("phoneNumber") String phoneNumber) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reservationService.getReservationInfo(reservationId, phoneNumber));
        } catch (ReservationException e) {
            log.error("예약 정보 조회 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @ApiOperation(value = "Send Authentication SMS", notes = "[문자 수신] 본인 인증 문자 수신")
    @GetMapping("/sms/authKey/{phoneNumber}")
    public ResponseEntity<SendMessageResponseDto> sendAuthKey(@PathVariable("phoneNumber") String phoneNumber) {
        SendMessageResponseDto responseDto = smsService.sendAuthenticationKeySms(phoneNumber);
        if (!responseDto.getStatusCode().equals("202")) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Authentication Validation", notes = "본인 인증 문자 입력")
    @GetMapping("/validation/authKey/{phoneNumber}/{authKey}")
    public ResponseEntity<Boolean> validateAuthKey(@PathVariable("phoneNumber") String phoneNumber, @PathVariable("authKey") String authKey) {
        return ResponseEntity.status(HttpStatus.OK).body(smsService.validateAuthenticationKey(phoneNumber, authKey));
    }

}
