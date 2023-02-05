package com.yeoyeo.entity;

import com.yeoyeo.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DateRoomTest {

    @Test
    public void test_setStateBooked() {
        // Given
        Room room1 = Room.builder().name("방1").build();
        Room room2 = Room.builder().name("방2").build();
        LocalDate now = LocalDate.now();
        DateRoom dateRoom1 = DateRoom.builder()
                .date(now)
                .room(room1)
                .build();
        DateRoom dateRoom2 = DateRoom.builder()
                .date(now)
                .room(room2)
                .build();

        // When
        dateRoom1.setStateBooked();

        // Then
        assertThat(dateRoom1.getRoomReservationState()).isEqualTo(1);
        assertThat(dateRoom2.getRoomReservationState()).isEqualTo(0);
    }

}
