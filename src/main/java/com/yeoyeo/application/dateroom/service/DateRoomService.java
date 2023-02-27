package com.yeoyeo.application.dateroom.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.dto.*;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.general.webclient.WebClientService;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DateRoomService extends Thread {

    private final DateRoomRepository dateRoomRepository;
    private final RoomRepository roomRepository;

    private final WebClientService webClientService;
    @Value("${data.holiday.key}")
    String holidayKey;

    public List<DateRoomInfoByDateDto> showAllDateRooms() {
        List<DateRoom> dateRoomList =  dateRoomRepository.findAllByOrderByDate();
        return getDateRoomInfoList(dateRoomList);
    }

    public DateRoom2MonthDto show2MonthsDateRooms(int year, int month) {
        LocalDate thisMonthStartDate = LocalDate.of(year, month, 1);
        LocalDate nextMonthStartDate = thisMonthStartDate.plusMonths(1);
        List<DateRoomInfoByDateDto> thisMonth = getDateRoomInfoList(getMonthDateRooms(thisMonthStartDate));
        List<DateRoomInfoByDateDto> nextMonth = getDateRoomInfoList(getMonthDateRooms(nextMonthStartDate));
        return new DateRoom2MonthDto(thisMonth, nextMonth);
    }

    private List<DateRoom> getMonthDateRooms(LocalDate firstMonthDate) {
        LocalDate lastMonthDate = firstMonthDate.plusMonths(1).minusDays(1);
        return dateRoomRepository.findAllByDateBetweenOrderByDate(firstMonthDate, lastMonthDate);
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
        // Todo - 존재하는 방이면 생성 X
        DateRoom dateRoom = DateRoom.builder()
                .date(date)
                .room(room)
                .webClientService(webClientService)
                .key(holidayKey)
                .build();
        dateRoomRepository.save(dateRoom);
    }

    @Transactional
    public String makeDateRoom(MakeDateRoomDto makeDateRoomDto) throws Exception {
        Room room = roomRepository.findById(makeDateRoomDto.getRoomId()).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        // Todo - 존재하는 방이면 생성 X
        DateRoom dateRoom = DateRoom.builder()
                .date(makeDateRoomDto.getDate())
                .room(room)
                .webClientService(webClientService)
                .key(holidayKey)
                .build();
        return dateRoomRepository.save(dateRoom).getDateRoomId();
    }

    @Transactional
    public GeneralResponseDto makeDateRoom(int year, int month, int day, long roomId) {
        try {
            LocalDate date = LocalDate.of(year, month, day);
            Room room = roomRepository.findById(roomId).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
            DateRoom dateRoom = DateRoom.builder()
                    .date(date)
                    .room(room)
                    .webClientService(webClientService)
                    .key(holidayKey)
                    .build();
             dateRoomRepository.save(dateRoom).getDateRoomId();
            return new GeneralResponseDto(false, -1, "방 생성에 성공했습니다.");
        } catch (Exception e) {
            log.error("방 생성 중 에러 발생", e);
            return new GeneralResponseDto(false, -1, "방 생성에 실패했습니다.");
        }
    }

    @Transactional
    public void setDateRoomUnReservableByDay(LocalDate date) {
        List<DateRoom> dateRoomList = dateRoomRepository.findAllByDate(date);
        dateRoomList.forEach(DateRoom::setUnReservable);
        dateRoomRepository.saveAll(dateRoomList);
    }

    private List<DateRoomInfoByDateDto> getDateRoomInfoList(List<DateRoom> dateRoomList) {
        List<DateRoomInfoByDateDto> dateRoomInfoByDateDtos = new ArrayList<>();
        dateRoomList.forEach(dateRoom -> {
            if (dateRoomInfoByDateDtos.size()==0) {
                DateRoomInfoByDateDto newDto = new DateRoomInfoByDateDto(dateRoom.getDate(), new DateRoomInfoDto(dateRoom));
                dateRoomInfoByDateDtos.add(newDto);
            } else {
                DateRoomInfoByDateDto lastDto = dateRoomInfoByDateDtos.get(dateRoomInfoByDateDtos.size()-1);
                if (lastDto.getDate().isEqual(dateRoom.getDate())) lastDto.addDateRoomInfo(new DateRoomInfoDto(dateRoom));
                else {
                    DateRoomInfoByDateDto newDto = new DateRoomInfoByDateDto(dateRoom.getDate(), new DateRoomInfoDto(dateRoom));
                    dateRoomInfoByDateDtos.add(newDto);
                }
            }
        });
        return dateRoomInfoByDateDtos;
    }

}
