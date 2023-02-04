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

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // AfterAll 어노테이션을 non-static으로 사용하기 위한 어노테이션
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RoomServiceTest {

    @Autowired
    RoomRepository roomRepository;
    @Autowired
    RoomService roomService;

    @AfterEach
    public void cleanup() { roomRepository.deleteAll(); }

    @AfterAll
    public void teardown() {
        roomRepository.deleteAll();
    }

    @Test
    public void test_makeRoom() {
        // Given
        String roomName = "왼쪽방";
        MakeRoomDto requestDto = MakeRoomDto.builder().name(roomName).build();

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
        String roomName1 = "왼쪽방";
        String roomName2 = "오른쪽방";
        Room room1 = Room.builder().name(roomName1).build();
        Room room2 = Room.builder().name(roomName2).build();
        roomRepository.save(room1);
        roomRepository.save(room2);

        // When
        List<RoomInfoDto> roomInfoDtoList = roomService.showAllRooms();

        // Then
        RoomInfoDto roomInfo1 = roomInfoDtoList.get(0);
        RoomInfoDto roomInfo2 = roomInfoDtoList.get(1);
        assertThat(roomInfo1.getRoomName()).isEqualTo(roomName1);
        assertThat(roomInfo2.getRoomName()).isEqualTo(roomName2);
    }

}
