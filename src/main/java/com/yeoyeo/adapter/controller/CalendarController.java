package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.common.method.CommonMethod;
import com.yeoyeo.application.message.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Api(tags = {"예약정보 동기화 API"})
@RequiredArgsConstructor
@RestController
@RequestMapping("calendar")
public class CalendarController {

    private final CommonMethod commonMethod;
    private final CalendarService calendarService;
    private final MessageService messageService;

    @ApiOperation(value = "Send to Platform from YeoYeo-A", notes = "(예약정보 동기화) [여유] 데이터 내보내기")
    @GetMapping("/sync/yeoyeo-Ab83sf0AD5$27Pk3-3!J4pjw")
    public void sendIcalendarData_A(HttpServletResponse response) {
        commonMethod.printIp("sendIcalendarData_A");
        sendIcalendarData(response, 1);
    }

    @ApiOperation(value = "Send to Platform from YeoYeo-B", notes = "(예약정보 동기화) [여행] 데이터 내보내기")
    @GetMapping("/sync/yeoyeo-Bk87wf0$D63q1P!3-2$H0pjw")
    public void sendIcalendarData_B(HttpServletResponse response) {
        commonMethod.printIp("sendIcalendarData_B  ");
        sendIcalendarData(response, 2);
    }

    @ApiOperation(value = "Sync-Airbnb-A", notes = "에어비앤비 A호실 예약정보 동기화")
    @PutMapping("/manual/airbnb/a")
    public void syncAirbnbA() { calendarService.syncInICSFile_Airbnb_A(); }

    @ApiOperation(value = "Sync-Airbnb-B", notes = "에어비앤비 B호실 예약정보 동기화")
    @PutMapping("/manual/airbnb/b")
    public void syncAirbnbB() { calendarService.syncInICSFile_Airbnb_B(); }



    @ApiOperation(value = "Read", notes = "수신된 에어비앤비 A호실 ICS 파일 읽기")
    @PostMapping("/manual/airbnb/a")
    public void readAirbnbA() {
        calendarService.readICSFile_Airbnb_A();
    }
    @ApiOperation(value = "Read", notes = "수신된 에어비앤비 A호실 ICS 파일 읽기")
    @PostMapping("/manual/airbnb/b")
    public void readAirbnbB() { calendarService.readICSFile_Airbnb_B(); }
    @ApiOperation(value = "Receive", notes = "에어비앤비 A호실 예약정보 수신")
    @GetMapping("/manual/airbnb/a")
    public void receiveAirbnbA() {
        calendarService.getICSFile_Airbnb_A();
    }
    @ApiOperation(value = "Receive", notes = "에어비앤비 A호실 예약정보 수신")
    @GetMapping("/manual/airbnb/b")
    public void receiveAirbnbB() {
        calendarService.getICSFile_Airbnb_B();
    }

    @ApiOperation(value = "Write", notes = "ICS 파일 쓰기")
    @PostMapping("/manual")
    public void manualWrite() {
        calendarService.writeICSFile(1);
        calendarService.writeICSFile(2);
    }

    private void sendIcalendarData(HttpServletResponse response, long roomId) {
        calendarService.writeICSFile(roomId);
        calendarService.sendICalendarData(response, roomId);
        messageService.sendNoticeMsgToConfirmedReservations(roomId);
    }

}
