package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.common.method.CommonMethod;
import com.yeoyeo.application.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "예약정보 동기화 API", description = "다른 플랫폼과 예약정보를 지속적으로 동기화하는 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("calendar")
public class CalendarController {

    private final CommonMethod commonMethod;
    private final CalendarService calendarService;
    private final MessageService messageService;

    @Operation(summary = "예약정보 플랫폼 송신 (여유)", description = "(예약정보 동기화) [여유] 데이터 내보내기")
    @GetMapping("/sync/yeoyeo-Ab83sf0AD5$27Pk3-3!J4pjw")
    public void sendIcalendarData_A(HttpServletResponse response) {
        commonMethod.printIp("sendIcalendarData_A");
        sendIcalendarData(response, 1);
    }

    @Operation(summary = "예약정보 플랫폼 송신 (여행)", description = "(예약정보 동기화) [여행] 데이터 내보내기")
    @GetMapping("/sync/yeoyeo-Bk87wf0$D63q1P!3-2$H0pjw")
    public void sendIcalendarData_B(HttpServletResponse response) {
        commonMethod.printIp("sendIcalendarData_B  ");
        sendIcalendarData(response, 2);
    }

    @Operation(summary = "예약정보 에어비앤비 수신 (여유)", description = "(예약정보 동기화) [여유] 에어비앤비 데이터 수신")
    @PutMapping("/manual/airbnb/a")
    public void syncAirbnbA() { calendarService.syncInICSFile_Airbnb_A_sync(); }

    @Operation(summary = "예약정보 에어비앤비 수신 (여행)", description = "(예약정보 동기화) [여행] 에어비앤비 데이터 수신")
    @PutMapping("/manual/airbnb/b")
    public void syncAirbnbB() { calendarService.syncInICSFile_Airbnb_B_sync(); }

    @Operation(summary = "예약정보 부킹닷컴 수신 (여행)", description = "(예약정보 동기화) [여행] 부킹닷컴 데이터 수신")
    @PutMapping("/manual/booking/b")
    public void syncBookingB() { calendarService.syncInICSFile_Booking_B_sync(); }

    @Operation(summary = "ICS 파일 읽기(에어비앤비, 여유)", description = "[여유] 수신된 에어비앤비 ICS 파일 읽기")
    @PostMapping("/manual/airbnb/a")
    public void readAirbnbA() {
        calendarService.readICSFile_Airbnb_A();
    }
    @Operation(summary = "ICS 파일 읽기(에어비앤비, 여행)", description = "[여행] 수신된 에어비앤비 ICS 파일 읽기")
    @PostMapping("/manual/airbnb/b")
    public void readAirbnbB() {
        calendarService.readICSFile_Airbnb_B();
    }
    @Operation(summary = "ICS 파일 수신(에어비앤비, 여유)", description = "[여유] 에어비앤비 예약정보 수신")
    @GetMapping("/manual/airbnb/a")
    public void receiveAirbnbA() {
        calendarService.getICSFile_Airbnb_A();
    }
    @Operation(summary = "ICS 파일 수신(에어비앤비, 여행)", description = "[여행] 에어비앤비 예약정보 수신")
    @GetMapping("/manual/airbnb/b")
    public void receiveAirbnbB() {
        calendarService.getICSFile_Airbnb_B();
    }

    @Operation(summary = "ICS 파일 읽기(부킹닷컴, 여행)", description = "[여행] 수신된 부킹닷컴 ICS 파일 읽기")
    @PostMapping("/manual/booking/b")
    public void readBookingB() {
        calendarService.readICSFile_Booking_B();
    }
    @Operation(summary = "ICS 파일 수신(부킹닷컴, 여행)", description = "[여행] 부킹닷컴 예약정보 수신")
    @GetMapping("/manual/booking/b")
    public void receiveBookingB() {
        calendarService.getICSFile_Booking_B();
    }

    @Operation(summary = "ICS 파일 쓰기 (홈페이지)", description = "ICS 파일 쓰기 (홈페이지 데이터)")
    @PostMapping("/manual")
    public void manualWrite() {
        calendarService.writeICSFile(1);
        calendarService.writeICSFile(2);
    }

    @Operation(summary = "ICS 파일 쓰기 (종합)", description = "ICS 파일 쓰기 (종합 데이터)")
    @PostMapping("/manual/full")
    public void writeFullICSFile() {
        calendarService.writeFullICSFile(1);
        calendarService.writeFullICSFile(2);
    }

    private void sendIcalendarData(HttpServletResponse response, long roomId) {
        calendarService.writeICSFile(roomId);
        calendarService.sendICalendarData(response, roomId);
        messageService.sendNoticeMsgToConfirmedReservations(roomId);
    }

}
