package com.yeoyeo.application.calendar.etc;

import com.yeoyeo.application.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class CalendarScheduler {

    private final CalendarService calendarService;

    @Transactional
    @Scheduled(cron = "33 33 0/3 * * *") // 3시간마다 도는 스케줄러
    public void regularSync_Airbnb() {
        log.info("[SCHEDULE - Regular Calendar Synchronization : Airbnb ]");
        calendarService.syncInICSFile_Airbnb_A();
        calendarService.syncInICSFile_Airbnb_B();
        calendarService.syncInICSFile_Booking_B();
    }

}
