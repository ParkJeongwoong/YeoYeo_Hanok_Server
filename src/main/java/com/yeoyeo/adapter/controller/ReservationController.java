package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.reservation.dto.AuthKeyResponseDto;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationHomeDto;
import com.yeoyeo.application.reservation.dto.MakeReservationRequestDto.MakeReservationHomeRequestDto;
import com.yeoyeo.application.reservation.dto.ReservationDetailInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "예약 API", description = "예약 관련 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final MessageService messageService;
    private final CalendarService calendarService;

    private final DateRoomRepository dateRoomRepository;

    @Operation(summary = "예약", description = "예약 - 아임포트 결제 모듈로 호출 전 예약 정보 생성 => merchant_uid 응답")
    @Transactional
    @PostMapping("/reserve")
    public ResponseEntity<GeneralResponseDto> createReservation(@RequestBody MakeReservationHomeRequestDto requestDto) {
        log.info("예약 정보 생성 요청");
        try {
            MakeReservationHomeDto reservationHomeDto = requestDto.getMakeReservationDto(dateRoomRepository);
            calendarService.syncInICSFile_Reservation(reservationHomeDto.getDateRoomList().get(0).getRoom().getId());
            Reservation reservation = reservationService.createReservation(reservationHomeDto);
            return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).resultId(reservation.getId()).message("예약 정보 생성 완료").build());
        } catch (ReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message(e.getMessage()).build());
        }
    }

    @Operation(summary = "예약 상세 정보 조회", description = "[예약 상세 정보] 예약 상세 정보 조회")
    @GetMapping("/detail/{reservationId}/{phoneNumber}")
    public ResponseEntity<ReservationDetailInfoDto> getReservationInfo(@PathVariable("reservationId") long reservationId, @PathVariable("phoneNumber") String phoneNumber) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(reservationService.getReservationInfo(reservationId, phoneNumber));
        } catch (ReservationException e) {
            log.error("예약 정보 조회 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "본인 인증 문자 발송", description = "[문자 수신] 본인 인증 문자 발송")
    @GetMapping("/sms/authKey/{phoneNumber}")
    public ResponseEntity<GeneralResponseDto> sendAuthKey(@PathVariable("phoneNumber") String phoneNumber) {
        AuthKeyResponseDto responseDto = messageService.sendAuthenticationKeyMsg(phoneNumber);
        if (!responseDto.getMessageStatusCode().equals("202")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message("본인 인증 문자 발송 실패").build());
        }
        if (responseDto.getToken() != null) { // Redis 연결 실패로 토큰을 이용한 인증키 저장
            Cookie cookie = new Cookie("authToken", responseDto.getToken());
            cookie.setPath("/reserve/validation/authKey");
            cookie.setMaxAge(60 * 3);
            return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(GeneralResponseDto.builder().success(true).message("본인 인증 문자 발송 완료").build());
        }
        return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).message("본인 인증 문자 발송 완료").build());
    }

    @Operation(summary = "본인 인증 문자 입력", description = "[문자 수신] 본인 인증 문자 입력")
    @GetMapping("/validation/authKey/{phoneNumber}/{authKey}")
    public ResponseEntity<Boolean> validateAuthKey(@PathVariable("phoneNumber") String phoneNumber, @PathVariable("authKey") String authKey, @CookieValue(value = "authToken", required = false) String authToken) {
        return ResponseEntity.status(HttpStatus.OK).body(messageService.validateAuthenticationKey(phoneNumber, authKey, authToken));
    }

    @Operation(summary = "예약 변경 수락", description = "예약 충돌로 인한 방 변경 수락")
//    @PutMapping("/offer/{reservationId}/accept")
    @GetMapping("/offer/{reservationId}/accept") // Todo : Offer Page 만든 후 Put으로 변경
    public ResponseEntity<GeneralResponseDto> acceptOffer(@PathVariable("reservationId") long reservationId) {
        try {
            reservationService.acceptOffer(reservationId);
            return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).message("예약 변경 수락 완료").build());
        } catch (ReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message(e.getMessage()).build());
        }
    }

    @Operation(summary = "예약 변경 거절", description = "예약 충돌로 인한 방 변경 거절")
//    @PutMapping("/offer/{reservationId}/reject")
    @GetMapping("/offer/{reservationId}/reject") // Todo : Offer Page 만든 후 Put으로 변경
    public ResponseEntity<GeneralResponseDto> rejectOffer(@PathVariable("reservationId") long reservationId) {
        try {
            reservationService.rejectOffer(reservationId);
            return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).message("예약 변경 거절 완료").build());
        } catch (ReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message(e.getMessage()).build());
        }
    }

}
