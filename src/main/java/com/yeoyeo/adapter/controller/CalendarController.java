package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.calendar.service.CalendarService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @ApiOperation(value = "Send to Platform from YeoYeo", notes = "(예약정보 동기화) 데이터 내보내기")
    @GetMapping("/download/yeoyeo-Ab87sf0AD635$PO3-3!E4pjw")
    public void sendingIcalendarData(HttpServletResponse response) {
        calendarService.sendICalendarData(response);
    }

    @ApiOperation(value = "Read", notes = "ICS 파일 읽기")
    @GetMapping("/test/1")
    public void test() {
        calendarService.syncAirbnbICSFile();
    }
    @ApiOperation(value = "Receive", notes = "에어비앤비 예약정보 수신")
    @GetMapping("/test/2")
    public void test2() {
        calendarService.getAirbnbICSFile();
    }
    @ApiOperation(value = "Write", notes = "ICS 파일 쓰기")
    @GetMapping("/test/3")
    public void test3() {
        calendarService.writeICSFile();
    }
    @ApiOperation(value = "Sync-Airbnb-A", notes = "에어비앤비 A호실 예약정보 동기화")
    @GetMapping("/test/airbnb/a")
    public void testAirbnbA() { calendarService.syncInICSFile_Airbnb_A(); }
    @ApiOperation(value = "Sync-Airbnb-B", notes = "에어비앤비 V호실 예약정보 동기화")
    @GetMapping("/test/airbnb/b")
    public void testAirbnbB() { calendarService.syncInICSFile_Airbnb_B(); }

}
