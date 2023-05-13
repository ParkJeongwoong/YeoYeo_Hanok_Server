package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.admin.dto.ChangeRoomDefaultPriceRequestDto;
import com.yeoyeo.application.admin.dto.SignupDto;
import com.yeoyeo.application.admin.service.AuthService;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.dto.ChangeDateRoomListPriceRequestDto;
import com.yeoyeo.application.dateroom.dto.ChangeDateRoomListStatusRequestDto;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.dto.MakeReservationAdminRequestDto;
import com.yeoyeo.application.reservation.dto.ReservationInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.application.room.service.RoomService;
import com.yeoyeo.domain.Reservation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"관리자 API"})
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("admin")
public class AdminController {

    private final AuthService authService;
    private final RoomService roomService;
    private final DateRoomService dateRoomService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;

    // TEST 용도
    @PostMapping("/signup")
    public String test2(@RequestBody SignupDto requestDto) {
        return authService.signup(requestDto);
    }

    // ROOM 관련
    @ApiOperation(value = "Change Default Price", notes = "방의 기본가(평일가격, 주말가격, 성수기 평일가격, 성수기 주말가격) 설정")
    @PutMapping("/room/{roomId}")
    public ResponseEntity<GeneralResponseDto> changeRoomDefaultPrice(@PathVariable long roomId, @RequestBody ChangeRoomDefaultPriceRequestDto requestDto) {
        GeneralResponseDto responseDto = roomService.changeRoomDefaultPrice(roomId, requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    // DATEROOM 관련
    @ApiOperation(value = "Change (Multiple) Date Rooms Price", notes = "배열 형태의 dateRoomId를 모아 dateroom 가격 일괄 변경 (priceType - 0 : 직접 설정, 1 : 주중, 2 : 주말, 3 : 성수기 주중, 4 : 성수기 주말)")
    @PutMapping("/dateroom/list/price")
    public ResponseEntity<GeneralResponseDto> changeDateRoomListPrice(@RequestBody ChangeDateRoomListPriceRequestDto requestDto) {
        GeneralResponseDto responseDto = dateRoomService.changeDateRoomListPrice(requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Change (Multiple) Date Rooms Status", notes = "배열 형태의 dateRoomId를 모아 dateroom 예약 상태 일괄 변경 (0 : 예약 가능, 1 : 예약 완료) *예약되지 않은 방만 변경 가능")
    @PutMapping("/dateroom/list/status")
    public ResponseEntity<GeneralResponseDto> changeDateRoomListStatus(@RequestBody ChangeDateRoomListStatusRequestDto requestDto) {
        GeneralResponseDto responseDto = dateRoomService.changeDateRoomListStatus(requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Dateroom Creation", notes = "문제가 발생해서 방 날짜 정보가 생성되지 않았을 때 사용하는 용도")
    @PostMapping("/dateroom/{year}/{month}/{day}/{roomId}")
    public ResponseEntity<GeneralResponseDto> createDateRoom(
            @PathVariable("year") int year, @PathVariable("month") int month, @PathVariable("day") int day, @PathVariable("roomId") long roomId
    ) { return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.makeDateRoom(year, month, day, roomId)); }

    // RESERVATION 관련
    @ApiOperation(value = "Reservation List", notes = "관리자의 예약 관리용 예약 정보 조회 (0 : 전체, 1 : 숙박 대기, 2 : 숙박 완료, 3 : 예약 취소, 4 : 환불 완료")
    @GetMapping("/reservation/list/{type}")
    public ResponseEntity<List<ReservationInfoDto>> showReservations(@PathVariable("type") int type) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.showReservations(type));
    }

    @ApiOperation(value = "Reservation", notes = "관리자의 해당 날짜 예약 불가능 처리")
    @Transactional
    @PostMapping("/reserve")
    public ResponseEntity<GeneralResponseDto> createReservation(@RequestBody MakeReservationAdminRequestDto requestDto) {
        try {
            Reservation reservation = reservationService.createReservation(requestDto);
            return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).resultId(reservation.getId()).message("예약 정보 생성 완료").build());
        } catch (ReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message(e.getMessage()).build());
        }
    }

    @ApiOperation(value = "Reservation Cancel", notes = "관리자의 해당 날짜 예약 취소 처리 (환불 미포함)")
    @DeleteMapping("/reservation/{reservationId}")
    public ResponseEntity<GeneralResponseDto> cancelReservation(@PathVariable("reservationId") long reservationId) {
        GeneralResponseDto responseDto = reservationService.cancel(reservationId);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    // PAYMENT 관련
    @ApiOperation(value = "Payment Refund", notes = "관리자의 예약 취소 및 환불 처리 (전액 환불)")
    @DeleteMapping("/payment/{reservationId}")
    public ResponseEntity<GeneralResponseDto> refundPayment(@PathVariable("reservationId") long reservationId) {
        GeneralResponseDto responseDto = paymentService.refundByAdmin(reservationId);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
