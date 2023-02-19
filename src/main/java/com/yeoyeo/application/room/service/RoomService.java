package com.yeoyeo.application.room.service;

import com.yeoyeo.application.room.dto.RoomInfoDto;
import com.yeoyeo.application.room.dto.MakeRoomDto;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public List<RoomInfoDto> showAllRooms() {
        return roomRepository.findAll().stream().map(RoomInfoDto::new).collect(Collectors.toList());
    }

    // 초기 방 생성 완료
//    @Transactional
//    public void makeRoom() {
//        roomRepository.save(Room.builder().name("A").build());
//        roomRepository.save(Room.builder().name("B").build());
//    }

    @Transactional
    public void makeRoom(String name, int price, int priceWeekend, int priceHoliday, int priceSpecial) {
        Room room = new Room(name, price, priceWeekend, priceHoliday, priceSpecial);
        roomRepository.save(room);
    }

    @Transactional
    public long makeRoom(MakeRoomDto requestDto) {
        Room room = new Room(requestDto);
        return roomRepository.save(room).getId();
    }

}
