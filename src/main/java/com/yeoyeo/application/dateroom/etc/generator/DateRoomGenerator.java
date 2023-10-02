package com.yeoyeo.application.dateroom.etc.generator;

import com.yeoyeo.application.dateroom.service.DateRoomService;
import com.yeoyeo.application.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class DateRoomGenerator {

    private final RoomService roomService;
    private final DateRoomService dateRoomService;

    @Transactional
    @PostConstruct
    private void init() {
        roomService.makeRoom(); // 초기 방 생성 완료
        dateRoomService.make9MonthsDateRoom();
        dailyRoomUnReservableJob();
    }

    @Transactional
    @Scheduled(cron = "3 0 0 * * *") // 매일 0시 0분 3초 동작
    protected void dailyRoomReservableJob() {
        log.info("[SCHEDULE - Daily Room Reservable Job]");
        LocalDate date = LocalDate.now().plusDays(180);
        log.info("180일 후 날짜 : {}", date);
        dateRoomService.setDateRoomReservableByDay(date);
    }

    @Transactional
    @Scheduled(cron = "10 0 0 * * *") // 매일 0시 0분 10초 동작
    protected void dailyRoomCreation() {
        log.info("[SCHEDULE - Daily Room Creation]");
        LocalDate date = LocalDate.now().plusDays(270);
        log.info("270일 후 날짜 : {}", date);
        try {
            dateRoomService.fetchHolidayData(date.getYear(), date.getMonthValue());
            dateRoomService.makeDateRoom(2, date);
            dateRoomService.makeDateRoom(1, date);
            dateRoomService.setDateRoomUnReservableByDay(date);
        } catch (Exception e) {
            log.error("방 날짜 생성 중 에러 발생", e);
        }
        log.info("방 날짜 생성 : {}", date);
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * 1")
    protected void weeklyDefaultPriceTypeReset() { // 매주 월요일 새벽 3시에 동작
        log.info("[SCHEDULE - Weekly Default Price-type Reset Job]");
        LocalDate today = LocalDate.now();
        dateRoomService.fetchHolidayData(today.getYear(), today.getMonthValue());
        dateRoomService.resetDateRoomPriceType_month(today);
    }

    @Transactional
    @Scheduled(cron = "0 30 5 * * *") // 매일 5시 30븐 0초 동작
    protected void dailyRoomUnReservableJob() {
        log.info("[SCHEDULE - Daily Room UnReservable Job]");
        dateRoomService.setDateRoomUnReservableByDay(LocalDate.now());
    }

}
