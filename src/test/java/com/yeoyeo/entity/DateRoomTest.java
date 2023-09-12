package com.yeoyeo.entity;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.HolidayRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DateRoomTest {

    @Autowired
    HolidayRepository holidayRepository;

    @Test
    public void test_setStateBooked() {
        // Given
        Room room1 = Room.builder().name("방1").build();
        Room room2 = Room.builder().name("방2").build();
        LocalDate now = LocalDate.now();
        DateRoom dateRoom1 = DateRoom.builder()
                .date(now)
                .room(room1)
                .holidayRepository(holidayRepository)
                .build();
        DateRoom dateRoom2 = DateRoom.builder()
                .date(now)
                .room(room2)
                .holidayRepository(holidayRepository)
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
