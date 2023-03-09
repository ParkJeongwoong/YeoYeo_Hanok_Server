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

    @ApiOperation(value = "Reservation List", notes = "(관리자용) 관리자의 예약 관리용 예약 정보 조회 (0 : 전체, 1 : 숙박 대기, 2 : 숙박 완료, 3 : 예약 취소, 4 : 환불 완료")
    @GetMapping("/list/{type}")
    public ResponseEntity<List<ReservationInfoDto>> showReservations(@PathVariable("type") int type) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.showReservations(type));
    }

    @ApiOperation(value = "Reservation Detail", notes = "(관리자용) 예약 상세 정보 조회")
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailInfoDto> getReservationInfo(@PathVariable("reservationId") long reservationId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reservationService.getReservationInfo(reservationId));
        } catch (ReservationException e) {
            log.error("예약 정보 조회 실패", e);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    @ApiOperation(value = "Reservation", notes = "(관리자용) 관리자의 해당 날짜 예약 취소 처리")
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<GeneralResponseDto> cancelReservation(@PathVariable("reservationId") long reservationId) {
        GeneralResponseDto responseDto = reservationService.cancel(reservationId);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Authentication", notes = "[문자 수신] 본인 인증 문자 수신")
    @GetMapping("/sms/authKey/{phoneNumber}")
    public ResponseEntity<SendMessageResponseDto> sendAuthKey(@PathVariable("phoneNumber") String phoneNumber) {
        SendMessageResponseDto responseDto = smsService.sendAuthenticationKeySms(phoneNumber);
        if (!responseDto.getStatusCode().equals("202")) ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Reservation", notes = "본인 인증 문자 입력")
    @GetMapping("/sms/authKey/{phoneNumber}/{authKey}")
    public ResponseEntity<Boolean> validateAuthKey(@PathVariable("phoneNumber") String phoneNumber, @PathVariable("authKey") String authKey) {
        return ResponseEntity.status(HttpStatus.OK).body(smsService.validateAuthenticationKey(phoneNumber, authKey));
    }

}
