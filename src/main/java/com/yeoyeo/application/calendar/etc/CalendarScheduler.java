package com.yeoyeo.application.calendar.etc;

import com.yeoyeo.application.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CalendarScheduler {

    private final CalendarService calendarService;

    @Scheduled(cron = "33 33 0/3 * * *") // 3시간마다 도는 스케줄러
    public void regularSync_Airbnb() {
        calendarService.syncInICSFile_Airbnb_A();
        calendarService.syncInICSFile_Airbnb_B();
    }

}
