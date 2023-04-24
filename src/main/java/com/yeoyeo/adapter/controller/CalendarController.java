package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.common.service.CommonMethods;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Api(tags = {"예약정보 동기화 API"})
@RequiredArgsConstructor
@RestController
@RequestMapping("calendar")
public class CalendarController {

    private final CommonMethods commonMethods;
    private final CalendarService calendarService;

    @ApiOperation(value = "Send to Platform from YeoYeo", notes = "(예약정보 동기화) 데이터 내보내기")
    @GetMapping("/download/yeoyeo-Ab87sf0AD635$PO3-3!E4pjw")
    public void sendingIcalendarData(HttpServletResponse response) {
        commonMethods.printIp("sendingIcalendarData");
        calendarService.writeICSFile();
        calendarService.sendICalendarData(response,1);
    }

    @ApiOperation(value = "Send to Platform from YeoYeo-A", notes = "(예약정보 동기화) [여유] 데이터 내보내기")
    @GetMapping("/sync/yeoyeo-Ab83sf0AD5$27Pk3-3!J4pjw")
    public void sendIcalendarData_A(HttpServletResponse response) {
        commonMethods.printIp("sendIcalendarData_A");
        calendarService.writeICSFile();
        calendarService.sendICalendarData(response,1);
    }

    @ApiOperation(value = "Send to Platform from YeoYeo-B", notes = "(예약정보 동기화) [여행] 데이터 내보내기")
    @GetMapping("/sync/yeoyeo-Bk87wf0$D63q1P!3-2$H0pjw")
    public void sendIcalendarData_B(HttpServletResponse response) {
        commonMethods.printIp("sendIcalendarData_B");
        calendarService.writeICSFile();
        calendarService.sendICalendarData(response,2);
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
