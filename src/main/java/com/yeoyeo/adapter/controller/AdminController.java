package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.admin.dto.AdminManageInfoRequestDto;
import com.yeoyeo.application.admin.dto.AdminManageInfoResponseDto;
import com.yeoyeo.application.admin.dto.ChangeRoomDefaultPriceRequestDto;
import com.yeoyeo.application.admin.dto.MessageTestRequestDto;
import com.yeoyeo.application.admin.dto.SignupDto;
import com.yeoyeo.application.admin.etc.exception.AdminManageInfoException;
import com.yeoyeo.application.admin.service.AdminManageService;
import com.yeoyeo.application.admin.service.AuthService;
import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.collision.service.OfferService;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.dto.ChangeDateRoomListPriceRequestDto;
import com.yeoyeo.application.dateroom.dto.ChangeDateRoomListStatusRequestDto;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import com.yeoyeo.application.message.dto.SendMessageResponseDto;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.dto.MakeReservationRequestDto.MakeReservationAdminRequestDto;
import com.yeoyeo.application.reservation.dto.MonthlyStatisticDto;
import com.yeoyeo.application.reservation.dto.ReservationInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.application.room.service.RoomService;
import com.yeoyeo.application.scraping.dto.ScrapingGetNaverRequestDto;
import com.yeoyeo.application.scraping.dto.ScrapingGetNaverResponseDto;
import com.yeoyeo.application.scraping.dto.ScrapingPostNaverRequestDto;
import com.yeoyeo.application.scraping.dto.ScrapingPostNaverResponseDto;
import com.yeoyeo.application.scraping.service.ScrapingService;
import com.yeoyeo.domain.Admin.Administrator;
import com.yeoyeo.domain.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 API", description = "관리자가 사용할 수 있는 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("admin")
public class AdminController {

    private final RoomService roomService;
    private final DateRoomService dateRoomService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final AdminManageService adminManageService;
    private final CalendarService calendarService;
    private final AuthService authService;
    private final MessageService messageService;
    private final OfferService offerService;
    private final ScrapingService scrapingService;

    // Auth
    @PostMapping("/signup")
    public String signup(@RequestBody SignupDto dto) {
        return authService.signup(dto);
    }

    // ROOM 관련
    @Operation(summary = "기본가 수정", description = "방의 기본가(평일가격, 주말가격, 성수기 평일가격, 성수기 주말가격) 설정")
    @PutMapping("/room/{roomId}")
    public ResponseEntity<GeneralResponseDto> changeRoomDefaultPrice(@PathVariable long roomId, @RequestBody ChangeRoomDefaultPriceRequestDto requestDto) {
        GeneralResponseDto responseDto = roomService.changeRoomDefaultPrice(roomId, requestDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    // DATEROOM 관련
    @Operation(summary = "날짜별 방 가격 일괄 변경", description = "배열 형태의 dateRoomId를 모아 dateroom 가격 일괄 변경 (priceType - 0 : 직접 설정, 1 : 주중, 2 : 주말, 3 : 성수기 주중, 4 : 성수기 주말)")
    @PutMapping("/dateroom/list/price")
    public ResponseEntity<GeneralResponseDto> changeDateRoomListPrice(@RequestBody ChangeDateRoomListPriceRequestDto requestDto) {
        GeneralResponseDto responseDto = dateRoomService.changeDateRoomListPrice(requestDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(summary = "날짜별 방 상태 일괄 변경", description = "배열 형태의 dateRoomId를 모아 dateroom 예약 상태 일괄 변경 (0 : 예약 가능, 1 : 예약 완료) *예약되지 않은 방만 변경 가능")
    @PutMapping("/dateroom/list/status")
    public ResponseEntity<GeneralResponseDto> changeDateRoomListStatus(@RequestBody ChangeDateRoomListStatusRequestDto requestDto) {
        GeneralResponseDto responseDto = dateRoomService.changeDateRoomListStatus(requestDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(summary = "날짜별 방 생성", description = "문제가 발생해서 방 날짜 정보가 생성되지 않았을 때 사용하는 용도")
    @PostMapping("/dateroom/{year}/{month}/{day}/{roomId}")
    public ResponseEntity<GeneralResponseDto> createDateRoom(
            @PathVariable("year") int year, @PathVariable("month") int month, @PathVariable("day") int day, @PathVariable("roomId") long roomId
    ) { return ResponseEntity.status(HttpStatus.OK).body(dateRoomService.makeDateRoom(year, month, day, roomId)); }

    // RESERVATION 관련
    @Operation(summary = "예약 정보 조회", description = "관리자의 예약 관리용 예약 정보 조회 (0 : 전체, 1 : 숙박 대기, 2 : 숙박 완료, 3 : 예약 취소, 4 : 환불 완료")
    @GetMapping("/reservation/list/{type}")
    public ResponseEntity<List<ReservationInfoDto>> showReservations(@PathVariable("type") int type) {
        return ResponseEntity.status(HttpStatus.OK).body(reservationService.showReservations(type));
    }

    @Operation(summary = "예약 정보 생성", description = "관리자의 해당 날짜 예약 불가능 처리")
    @Transactional
    @PostMapping("/reserve")
    public ResponseEntity<GeneralResponseDto> createReservation(@RequestBody MakeReservationAdminRequestDto requestDto,
        @AuthenticationPrincipal Administrator administrator) {
        try {
            if (administrator != null) {
                requestDto.setAdministrator(administrator);
            }
            Reservation reservation = reservationService.createReservation(requestDto);
            return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).resultId(reservation.getId()).message("예약 정보 생성 완료").build());
        } catch (ReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GeneralResponseDto.builder().success(false).message(e.getMessage()).build());
        }
    }

    @Operation(summary = "예약 취소(환불 미포함)", description = "관리자의 해당 날짜 예약 취소 처리 (환불 미포함)")
    @DeleteMapping("/reservation/{reservationId}")
    public ResponseEntity<GeneralResponseDto> cancelReservation(@PathVariable("reservationId") long reservationId) {
        GeneralResponseDto responseDto = reservationService.cancel(reservationId);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    // PAYMENT 관련
    @Operation(summary = "전액 환불 처리", description = "관리자의 예약 취소 및 환불 처리 (전액 환불)")
    @DeleteMapping("/payment/{reservationId}")
    public ResponseEntity<GeneralResponseDto> refundPayment(@PathVariable("reservationId") long reservationId) {
        GeneralResponseDto responseDto = paymentService.refundByAdmin(reservationId);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    // ADMIN MANAGE INFO 관련
    @GetMapping("/manage/info")
    public ResponseEntity<List<AdminManageInfoResponseDto>> getAdminManageInfoList() {
        List<AdminManageInfoResponseDto> getAdminMangeInfoList = adminManageService.getAdminManageInfoList(LocalDate.now().minusDays(3));
        return ResponseEntity.status(HttpStatus.OK).body(getAdminMangeInfoList);
    }

    @PostMapping("/manage/info")
    public void addAdminManageInfo(@RequestBody AdminManageInfoRequestDto requestDto) {
        try {
            adminManageService.addAdminManageInfo(requestDto);
        } catch (AdminManageInfoException e) {
            log.error("AdminManageInfo 추가 실패", e);
        }
    }

    @PutMapping("/manage/info")
    public void setAdminManageInfo(@RequestBody AdminManageInfoRequestDto requestDto) {
        adminManageService.setAdminManageInfo(requestDto);
    }

    @DeleteMapping("/manage/info")
    public void deactivateAdminManageInfo(@RequestBody AdminManageInfoRequestDto requestDto) {
        adminManageService.deactivateAdminManageInfo(requestDto);
    }

    @PostMapping("/manage/info/list")
    public void createAdminManageInfoList() {
        calendarService.syncInICSFile_All();
        adminManageService.createAdminManageInfoList();
    }

    @Transactional
    @PostMapping("/manage/message/notice")
    public void sendAdminManageInfoNoticeMessage(@RequestBody AdminManageInfoRequestDto requestDto) {
        reservationService.getReservation(requestDto.getReservationId()).setManagementLevel(2);
        messageService.sendNoticeMsg(requestDto.getNumberOnlyPhoneNumber());
    }

    // Information 관련
    @GetMapping("/statistics/{year}/{month}")
    public ResponseEntity<List<MonthlyStatisticDto>> getStatistics(@PathVariable("year") int year, @PathVariable("month") int month) {
        List<MonthlyStatisticDto> monthlyStatisticDtoList = reservationService.getMonthlyStatistic(year, month);
        log.info("monthlyStatisticDtoList : {}", monthlyStatisticDtoList.size());
        return ResponseEntity.status(HttpStatus.OK).body(monthlyStatisticDtoList);
    }

    // TEST
    @PostMapping("/message/checkInMsg")
    public SendMessageResponseDto testCheckInMsg(@RequestBody MessageTestRequestDto requestDto) {
        return messageService.sendCheckInMsg(requestDto.getNumberOnlyPhoneNumber(), requestDto.getRoomName());
    }
    @PostMapping("/message/noticeMsg")
    public SendMessageResponseDto testNoticeMsg(@RequestBody MessageTestRequestDto requestDto) {
        return messageService.sendNoticeMsg(requestDto.getNumberOnlyPhoneNumber());
    }

    @GetMapping("/offer")
    public List<Long> getReservationList() {
        return offerService.getReservationIdList();
    }

    @GetMapping("/scraping/test")
    public String testScrapingServer() {
        try {
            return scrapingService.TestConnection();
        } catch (Exception e) {
            log.error("Scraping Test Error", e);
            return e.getMessage();
        }
    }

    @GetMapping("/scraping/sync/out")
    public ScrapingGetNaverResponseDto testScrapingSyncOut(@RequestParam("monthSize") int monthSize) {
        try {
            ScrapingGetNaverRequestDto requestDto = new ScrapingGetNaverRequestDto(monthSize);
            return scrapingService.GetReservationFromNaver(requestDto);
        } catch (Exception e) {
            log.error("Scraping Sync Out Error", e);
            return ScrapingGetNaverResponseDto.builder().message(e.getMessage()).build();
        }
    }

    @GetMapping("/scraping/save/in")
    public String testSaveFromNaver(@RequestParam("monthSize") int monthSize) {
        try {
            ScrapingGetNaverRequestDto requestDto = new ScrapingGetNaverRequestDto(monthSize);
            scrapingService.SyncReservationFromNaver(requestDto);
            return "Success";
        } catch (Exception e) {
            log.error("Scraping Sync Out Error", e);
            return e.getMessage();
        }
    }

    @GetMapping("/scraping/sync/in")
    public ScrapingPostNaverResponseDto testScrapingSyncIn(@RequestParam("date") String date, @RequestParam("roomId") long roomId) {
        try {
            String roomName = roomId == 1 ? "Yeoyu" : "Yeohang";
            ScrapingPostNaverRequestDto requestDto = ScrapingPostNaverRequestDto.builder().targetRoom(roomName).targetDateStr(date).build();
            return scrapingService.PostReservationFromNaverAsync(requestDto);
        } catch (Exception e) {
            log.error("Scraping Sync In Error", e);
            return ScrapingPostNaverResponseDto.builder().message(e.getMessage()).build();
        }
    }
}
