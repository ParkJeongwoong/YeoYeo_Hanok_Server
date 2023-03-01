package com.yeoyeo.application.dateroom;

import com.yeoyeo.application.dateroom.dto.DateRoomInfoDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoByDateDto;
import com.yeoyeo.application.dateroom.dto.MakeDateRoomDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.dateroom.service.DateRoomService;
import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // BeforeAll 어노테이션을 non-static으로 사용하기 위한 어노테이션
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DateRoomServiceTest {

    private WebClientService webClientService;
    @Value("${data.holiday.key}")
    String holidayKey;

    @Autowired
    DateRoomRepository dateRoomRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    DateRoomService dateRoomService;

    long roomId1 = 1;
    long roomId2 = 2;

    @AfterEach
    public void cleanup() {
        List<DateRoom> dateRooms = dateRoomRepository.findAll();
        dateRooms.forEach(dateRoom -> {
            try {
                if (dateRoom.getRoomReservationState()==1) {
                        dateRoom.resetState();
                }
            } catch (RoomReservationException e) {
                log.error("Dateroom 초기화 에러", e);
            }
        });
        dateRoomRepository.saveAll(dateRooms);
    }

    @Test
    @Transactional // DateRoom - Room의 관계가 ManyToOne, FetchType.LAZY이기 때문에 could not initialize proxy - no Session 에러 발생
    public void test_makeDateRoom() throws Exception {
        // Given
        LocalDate now = LocalDate.now();
        MakeDateRoomDto requestDto = MakeDateRoomDto.builder().date(now).roomId(roomId2).build();

        // When
        String dateRoomId = dateRoomService.makeDateRoom(requestDto);

        // Then
        DateRoom dateRoom = dateRoomRepository.findById(dateRoomId).orElseThrow(NoSuchElementException::new);
        Room room = roomRepository.findById(roomId2).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        assertThat(dateRoom.getDate()).isEqualTo(now);
        assertThat(dateRoom.getRoom()).isEqualTo(room);
        assertThat(dateRoom.getRoomReservationState()).isEqualTo(0);
    }

    @Test
    @Transactional // DateRoom - Room의 관계가 ManyToOne, FetchType.LAZY이기 때문에 could not initialize proxy - no Session 에러 발생
    public void test_showAllDateRooms() throws Exception {
        // Given
        LocalDate now = LocalDate.now();
        Room room1 = roomRepository.findById(roomId1).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        Room room2 = roomRepository.findById(roomId2).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
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

        dateRoomRepository.save(dateRoom1);
        dateRoomRepository.save(dateRoom2);

        // When
        List<DateRoomInfoByDateDto> dateRoomInfoByDateDtoList = dateRoomService.showAllDateRooms();

        // Then
        DateRoomInfoDto dateRoomInfo1 = dateRoomInfoByDateDtoList.get(0).getRooms().get(0);
        DateRoomInfoDto dateRoomInfo2 = dateRoomInfoByDateDtoList.get(0).getRooms().get(1);
        assertThat(dateRoomInfoByDateDtoList.get(0).getDate()).isEqualTo(now);
        assertThat(dateRoomInfo1.getRoomId()).isEqualTo(roomId1);
        assertThat(dateRoomInfo1.getReservationState()).isEqualTo(0);
        assertThat(dateRoomInfo2.getRoomId()).isEqualTo(roomId2);
        assertThat(dateRoomInfo2.getReservationState()).isEqualTo(0);
    }

}
