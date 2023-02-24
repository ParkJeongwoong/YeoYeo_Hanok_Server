package com.yeoyeo.entity;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DateRoomTest {

    private WebClientService webClientService;
    @Value("${data.holiday.key}")
    String holidayKey;

    @Test
    public void test_setStateBooked() {
        // Given
        Room room1 = Room.builder().name("방1").build();
        Room room2 = Room.builder().name("방2").build();
        LocalDate now = LocalDate.now();
        DateRoom dateRoom1 = DateRoom.builder()
                .date(now)
                .room(room1)
                .webClientService(webClientService)
                .key(holidayKey)
                .build();
        DateRoom dateRoom2 = DateRoom.builder()
                .date(now)
                .room(room2)
                .webClientService(webClientService)
                .key(holidayKey)
                .build();

        // When
        try {
            dateRoom1.setStateBooked();
        } catch (RoomReservationException e) {
            log.error("Dateroom 상태 변경 에러", e);
        }

        // Then
        assertThat(dateRoom1.getRoomReservationState()).isEqualTo(1);
        assertThat(dateRoom2.getRoomReservationState()).isEqualTo(0);
    }

}
