package com.yeoyeo.application.room.service;

import com.yeoyeo.application.room.dto.MakeRoomDto;
import com.yeoyeo.application.room.dto.RoomInfoDto;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.Room;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // AfterAll 어노테이션을 non-static으로 사용하기 위한 어노테이션
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RoomServiceTest {

    @Autowired
    RoomRepository roomRepository;
    @Autowired
    RoomService roomService;

    @Test
    public void test_makeRoom() {
        // Given
        String roomName = "왼쪽방";
        MakeRoomDto requestDto = MakeRoomDto.builder().name(roomName).price(250000).priceWeekend(300000).priceHoliday(330000).priceSpecial(220000).build();

        // When
        long roomId = roomService.makeRoom(requestDto);

        // Then
        Room room = roomRepository.findById(roomId).orElse(null);
        assert room != null;
        assertThat(room.getName()).isEqualTo(roomName);
    }

    @Test
    public void test_showAllRooms() {
        // Given
        Room room1 = roomRepository.findById(1L).orElseThrow(NoSuchElementException::new);
        Room room2 = roomRepository.findById(2L).orElseThrow(NoSuchElementException::new);
        String roomName1 = room1.getName();
        String roomName2 = room2.getName();

        // When
        List<RoomInfoDto> roomInfoDtoList = roomService.showAllRooms();

        // Then
        RoomInfoDto roomInfo1 = roomInfoDtoList.get(0);
        RoomInfoDto roomInfo2 = roomInfoDtoList.get(1);
        assertThat(roomInfo1.getRoomName()).isEqualTo(roomName1);
        assertThat(roomInfo2.getRoomName()).isEqualTo(roomName2);
    }

}
