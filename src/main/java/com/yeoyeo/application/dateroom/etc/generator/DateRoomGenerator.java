package com.yeoyeo.application.dateroom.etc.generator;

import com.yeoyeo.application.dateroom.service.DateRoomService;
import com.yeoyeo.application.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class DateRoomGenerator {

    private final RoomService roomService;
    private final DateRoomService dateRoomService;

    @PostConstruct
    private void init() {
//        roomService.makeRoom(); // 초기 방 생성 완료
        dateRoomService.make6MonthsDateRoom();
    }

    @Scheduled(cron = "10 0 0 * * *")
    private void dailyRoomCreation() {
        log.info("Daily Room Creation");
        LocalDate date = LocalDate.now().plusDays(90);
        try {
            dateRoomService.makeDateRoom(2, date);
            dateRoomService.makeDateRoom(1, date);
        } catch (Exception e) {
            log.error("방 날짜 생성 중 에러 발생", e);
        }
        log.info("방 날짜 생성 : {}", date);
    }
}
