package com.yeoyeo.application.room.service;

import com.yeoyeo.application.admin.dto.ChangeRoomDefaultPriceRequestDto;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.room.dto.RoomInfoDto;
import com.yeoyeo.application.room.dto.MakeRoomDto;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final DateRoomRepository dateRoomRepository;

    public List<RoomInfoDto> showAllRooms() {
        return roomRepository.findAll().stream().map(RoomInfoDto::new).collect(Collectors.toList());
    }

    // 초기 방 생성 완료로 사용 X
    @Transactional
    public void makeRoom() {
        if (roomRepository.findByName("여유")==null) roomRepository.save(Room.builder().name("여유").build());
        if (roomRepository.findByName("여행")==null) roomRepository.save(Room.builder().name("여행").build());
    }

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

    @Transactional
    public GeneralResponseDto changeRoomDefaultPrice(long roomId, ChangeRoomDefaultPriceRequestDto requestDto) {
        try {
            Room room = roomRepository.findById(roomId).orElseThrow(NoSuchElementException::new);
            room.changeDefaultPrice(requestDto);
            List<DateRoom> dateRooms = dateRoomRepository.findAll();
            dateRooms.forEach(DateRoom::setPrice);
            return GeneralResponseDto.builder().success(true).build();
        } catch (NoSuchElementException noSuchElementException) {
            return GeneralResponseDto.builder().success(false).message("존재하지 않는 방입니다.").build();
        }
    }

}
