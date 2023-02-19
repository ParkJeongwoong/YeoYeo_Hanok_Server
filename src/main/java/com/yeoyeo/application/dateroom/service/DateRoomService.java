package com.yeoyeo.application.dateroom.service;

import com.yeoyeo.application.dateroom.dto.*;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DateRoomService extends Thread {

    private final DateRoomRepository dateRoomRepository;
    private final RoomRepository roomRepository;

    public List<DateRoomInfoDto> showAllDateRooms() {
        return dateRoomRepository.findAll().stream().map(DateRoomInfoDto::new).collect(Collectors.toList());
    }

    public List<DateRoomInfoDto> show2MonthsDateRooms(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month+2, 1).minusDays(1);
        return dateRoomRepository.findAllByDateBetween(startDate, endDate).stream().map(DateRoomInfoDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void make6MonthsDateRoom() {
        LocalDate date = LocalDate.now();
        log.info("TODAY : {}", date);
        for (int i=0;i<180;i++) {
            if (dateRoomRepository.findByDateRoomId(date.toString().replaceAll("[^0-9]","")+"1")!=null) continue;
            try {
                makeDateRoom(2, date);
                makeDateRoom(1, date);
            } catch (Exception e) {
                log.error("초기 6개월치 방 날짜 생성 중 에러 발생", e);
            }
            date = date.plusDays(1);
        }
        log.info("Last Day : {}", date);
    }

    @Transactional
    public void makeDateRoom(long roomId, LocalDate date) throws Exception {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        DateRoom dateRoom = DateRoom.builder()
                .date(date)
                .room(room)
                .build();
        dateRoomRepository.save(dateRoom);
    }

    @Transactional
    public String makeDateRoom(MakeDateRoomDto makeDateRoomDto) throws Exception {
        Room room = roomRepository.findById(makeDateRoomDto.getRoomId()).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        DateRoom dateRoom = DateRoom.builder()
                .date(makeDateRoomDto.getDate())
                .room(room)
                .build();
        return dateRoomRepository.save(dateRoom).getDateRoomId();
    }

}
