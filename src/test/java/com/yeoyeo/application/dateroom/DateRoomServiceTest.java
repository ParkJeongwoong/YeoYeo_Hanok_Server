package com.yeoyeo.application.dateroom;

import com.yeoyeo.application.dateroom.dto.DateRoomInfoByDateDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.dateroom.repository.HolidayRepository;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // BeforeAll 어노테이션을 non-static으로 사용하기 위한 어노테이션
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DateRoomServiceTest {

    @Autowired
    DateRoomRepository dateRoomRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    HolidayRepository holidayRepository;
    @Autowired
    DateRoomService dateRoomService;

    long roomId1 = 1;
    long roomId2 = 2;

    @AfterEach
    public void cleanup() {
        List<DateRoom> dateRooms = dateRoomRepository.findAll();
        dateRooms.forEach(dateRoom -> {
            if (dateRoom.getRoomReservationState()==1) {
                    dateRoom.resetState();
            }
        });
        dateRoomRepository.saveAll(dateRooms);
    }

    @Test
    @Transactional // DateRoom - Room의 관계가 ManyToOne, FetchType.LAZY이기 때문에 could not initialize proxy - no Session 에러 발생
    public void test_makeDateRoom() throws Exception {
        // Given
        LocalDate testDate = LocalDate.now().plusYears(1);

        // When
        String dateRoomId = dateRoomService.makeDateRoom(testDate.getYear(), testDate.getMonthValue(), testDate.getDayOfMonth(), roomId2).getMessage();

        // Then
        DateRoom dateRoom = dateRoomRepository.findById(dateRoomId).orElseThrow(NoSuchElementException::new);
        Room room = roomRepository.findById(roomId2).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        assertThat(dateRoom.getDate()).isEqualTo(testDate);
        assertThat(dateRoom.getRoom()).isEqualTo(room);
        assertThat(dateRoom.getRoomReservationState()).isEqualTo(0);
    }

    @Test
    @Transactional // DateRoom - Room의 관계가 ManyToOne, FetchType.LAZY이기 때문에 could not initialize proxy - no Session 에러 발생
    public void test_showAllDateRooms() throws Exception {
        // Given
        LocalDate testDate = LocalDate.now().plusYears(2);
        Room room1 = roomRepository.findById(roomId1).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        Room room2 = roomRepository.findById(roomId2).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        DateRoom dateRoom1 = DateRoom.builder()
                .date(testDate)
                .room(room1)
                .holidayRepository(holidayRepository)
                .build();
        DateRoom dateRoom2 = DateRoom.builder()
                .date(testDate)
                .room(room2)
                .holidayRepository(holidayRepository)
                .build();

        List<DateRoom> dateRoomList = dateRoomRepository.findAllByDate(testDate);
        log.info("SIZE {}", dateRoomList.size());
        for (DateRoom dateRoom:dateRoomList) log.info("ID : {}", dateRoom.getId());
        if (!dateRoomRepository.findById(dateRoom1.getId()).isPresent()) {
            dateRoomRepository.save(dateRoom1);
        } else {
            log.info("이미 존재하는 방입니다. {}", dateRoom1.getId());
        }
        if (!dateRoomRepository.findById(dateRoom2.getId()).isPresent()) {
            dateRoomRepository.save(dateRoom2);
        } else {
            log.info("이미 존재하는 방입니다. {}", dateRoom2.getId());
        }

        // When
        List<DateRoomInfoByDateDto> dateRoomInfoByDateDtoList = dateRoomService.showAllDateRooms();
        // order dateRoomInfoByDateDtoList by roomId asc
        dateRoomInfoByDateDtoList.sort(Comparator.comparingLong(o -> o.getRooms().get(0).getRoomId()));

        // Then
        int length = dateRoomInfoByDateDtoList.size();
        DateRoomInfoDto dateRoomInfo1 = dateRoomInfoByDateDtoList.get(length-1).getRooms().get(0);
        DateRoomInfoDto dateRoomInfo2 = dateRoomInfoByDateDtoList.get(length-1).getRooms().get(1);
        assertThat(dateRoomInfoByDateDtoList.get(length-1).getDate()).isEqualTo(testDate);
        assertThat(dateRoomInfo1.getRoomId()).isEqualTo(roomId1);
        assertThat(dateRoomInfo1.getReservationState()).isEqualTo(0);
        assertThat(dateRoomInfo2.getRoomId()).isEqualTo(roomId2);
        assertThat(dateRoomInfo2.getReservationState()).isEqualTo(0);
    }

}
