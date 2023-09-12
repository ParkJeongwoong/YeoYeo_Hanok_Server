package com.yeoyeo.application.dateroom.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.common.service.WebClientService;
import com.yeoyeo.application.dateroom.dto.*;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.dateroom.repository.HolidayRepository;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DateRoomService extends Thread {

    private final RoomRepository roomRepository;
    private final DateRoomRepository dateRoomRepository;
    private final HolidayRepository holidayRepository;

    private final WebClientService webClientService;
    @Value("${data.holiday.key}")
    String holidayKey;

    public List<DateRoomInfoByDateDto> showAllDateRooms() {
        List<DateRoom> dateRoomList =  dateRoomRepository.findAllByOrderByDateAscRoom_Id();
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
        return dateRoomRepository.findAllByDateBetweenOrderByDateAscRoom_Id(firstMonthDate, lastMonthDate);
    }

    // 공휴일은 추가만 됨
    public void fetchHolidayData(int year, int month) {
        String year_str = String.valueOf(year);
        String month_str = month>9 ? String.valueOf(month) : "0"+month;
        String url = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?solYear="+year_str+"&solMonth="+month_str+"&_type=json&ServiceKey="+holidayKey;
        JSONParser parser = new JSONParser();

        JSONObject response = webClientService.get("application/json;charset=UTF-8", url, MediaType.TEXT_XML);
        if (response == null) { throw new RuntimeException("Return 데이터 문제"); }
        try {
            response = (JSONObject) parser.parse(response.toString().replaceAll("\"","\\\""));
            JSONObject res = (JSONObject) response.get("response");
            JSONObject body = (JSONObject) res.get("body");
            String totalCount = String.valueOf(body.get("totalCount"));
            if (totalCount.equals("0")) return;
            JSONObject items = (JSONObject) body.get("items");
            if (totalCount.equals("1")) { // 1개면 그냥 객체로 응답됨
                JSONObject jsonObject = (JSONObject) items.get("item");
                String dateString = String.valueOf(jsonObject.get("locdate"));
                String name = String.valueOf(jsonObject.get("dateName"));
                LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
                Holiday holiday = Holiday.builder().date(date).name(name).build();
                holidayRepository.save(holiday);
            } else { // 2개 이상이면 배열로 응답됨
                JSONArray holidays = (JSONArray) items.get("item");
                for (Object jsonObject : holidays) {
                    String dateString = String.valueOf(((JSONObject) jsonObject).get("locdate"));
                    String name = String.valueOf(((JSONObject) jsonObject).get("dateName"));
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    Holiday holiday = Holiday.builder().date(date).name(name).build();
                    holidayRepository.save(holiday);
                }
            }
        } catch (ParseException e) {
            log.error("공휴일 공공 데이터 확인 중 JSON Parsing Error 발생", e);
        }
    }

    @Transactional
    public void make9MonthsDateRoom() {
        long reservableDay = 180;
        int creatingDay = 270;
        int creatingMonth = 0;
        LocalDate date = LocalDate.now();
        LocalDate reservableDate = LocalDate.now().plusDays(reservableDay);
        log.info("TODAY : {}", date);
        for (int i=0;i<creatingDay;i++) {
            if (creatingMonth != date.getMonthValue()) {
                creatingMonth = date.getMonthValue();
                fetchHolidayData(date.getYear(), date.getMonthValue());
            }
            String preDateRoomId = date.toString().replaceAll("[^0-9]", "");
            DateRoom dateRoom1_found = dateRoomRepository.findById(preDateRoomId+"1").orElse(null);
            if (dateRoom1_found == null) {
                try {
                    DateRoom dateRoom1 = makeDateRoom(1, date);
                    DateRoom dateRoom2 = makeDateRoom(2, date);
                    log.info("발 생성 날짜 완료 날짜 : {}", date);
                    if (date.isAfter(reservableDate)) {
                        dateRoom1.setUnReservable();
                        dateRoom2.setUnReservable();
                    }
                } catch (Exception e) {
                    log.error("초기 9개월치 방 날짜 생성 중 에러 발생 - {}", preDateRoomId, e);
                }
            } else {
                DateRoom dateRoom2_found = dateRoomRepository.findById(preDateRoomId+"2").orElse(null);
                if (dateRoom2_found != null) {
                    dateRoom1_found.resetDefaultPriceType(holidayRepository);
                    dateRoom2_found.resetDefaultPriceType(holidayRepository);
                } else {
                    log.error("초기 9개월치 방 날짜 업데이트 중 에러 발생 - {}", preDateRoomId);
                }
            }
            date = date.plusDays(1);
        }
        log.info("Last Day : {}", date);
        dateRoomRepository.flush();
    }

    // #1 일반 방 생성 용도
    @Transactional
    public DateRoom makeDateRoom(long roomId, LocalDate date) throws Exception {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
        String dateRoomId = date.toString().replaceAll("[^0-9]","")+roomId;
        DateRoom foundDateRoom = dateRoomRepository.findById(dateRoomId).orElse(null);
        if (foundDateRoom == null) {
            DateRoom dateRoom = DateRoom.builder()
                    .date(date)
                    .room(room)
                    .holidayRepository(holidayRepository)
                    .build();
            return dateRoomRepository.save(dateRoom);
        } else {
            log.info("이미 존재하는 방입니다. {}", dateRoomId);
            return foundDateRoom;
        }
    }

    // #2 AdminController DateRoom 수동 생성 용도
    @Transactional
    public GeneralResponseDto makeDateRoom(int year, int month, int day, long roomId) {
        try {
            LocalDate date = LocalDate.of(year, month, day);
            Room room = roomRepository.findById(roomId).orElseThrow(()->new Exception("존재하지 않는 방입니다."));
            DateRoom dateRoom = DateRoom.builder()
                    .date(date)
                    .room(room)
                    .holidayRepository(holidayRepository)
                    .build();
            dateRoomRepository.save(dateRoom);
            return new GeneralResponseDto(false, 1, dateRoom.getId());
        } catch (Exception e) {
            log.error("방 생성 중 에러 발생", e);
            return new GeneralResponseDto(false, 0, "방 생성에 실패했습니다.");
        }
    }

    @Transactional
    public void setDateRoomUnReservableByDay(LocalDate date) {
        List<DateRoom> dateRoomList = dateRoomRepository.findAllByDate(date);
        dateRoomList.forEach(DateRoom::setUnReservable);
        dateRoomRepository.saveAll(dateRoomList);
    }

    @Transactional
    public void setDateRoomReservableByDay(LocalDate date) {
        List<DateRoom> dateRoomList = dateRoomRepository.findAllByDate(date);
        dateRoomList.forEach(DateRoom::setReservable);
        dateRoomRepository.saveAll(dateRoomList);
    }

    @Transactional
    public void resetDateRoomPriceType_month(LocalDate startDate) {
        LocalDate endDate = startDate.plusMonths(1);
        List<DateRoom> dateRoomList = dateRoomRepository.findAllByDateBetween(startDate, endDate);
        dateRoomList.forEach(dateRoom->dateRoom.resetDefaultPriceType(holidayRepository));
        dateRoomRepository.saveAll(dateRoomList);
    }

    // 여기엔 인원수에 따른 추가금이 포함되어 있지 않음
    public DateRoomPriceInfoDto getTotalPrice(long roomId, String checkInDate, String checkOutDate) {
        LocalDate startDate = LocalDate.parse(checkInDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate = LocalDate.parse(checkOutDate, DateTimeFormatter.ofPattern("yyyyMMdd")).minusDays(1);
        List<DateRoom> dateRoomList = dateRoomRepository.findAllByDateBetweenAndRoom_Id(startDate, endDate, roomId);
        List<DateRoomIdPriceInfoDto> infoDtoList = dateRoomList.stream().map(DateRoomIdPriceInfoDto::new).collect(Collectors.toList());
        int totalPrice;
        int originalPrice = 0;
        for (DateRoom dateRoom:dateRoomList) originalPrice += dateRoom.getPrice();
        totalPrice = originalPrice - 20000*(dateRoomList.size()-1);
        return new DateRoomPriceInfoDto(totalPrice, originalPrice, 20000*(dateRoomList.size()-1), dateRoomList.size(), infoDtoList);
    }

    @Transactional
    public GeneralResponseDto changeDateRoomListPrice(ChangeDateRoomListPriceRequestDto requestDto) {
        List<String> dateRoomIdList = requestDto.getDateRoomIdList();
        log.info("CHANGING DATEROOM PRICE {}", dateRoomIdList.toString());
        int price = requestDto.getPrice();
        int priceType = requestDto.getPriceType();
        List<DateRoom> dateRoomList = dateRoomRepository.findAllById(dateRoomIdList);
        if (dateRoomList.size()==0) return GeneralResponseDto.builder().success(false).message("유효한 dateroomId가 아닙니다.").build();
        for (DateRoom dateRoom:dateRoomList) {
            if (priceType == 0 && price>0) dateRoom.changePrice(price);
            else dateRoom.changePriceType(priceType);
        }
        return GeneralResponseDto.builder().success(true).build();
    }

    @Transactional
    public GeneralResponseDto changeDateRoomListStatus(ChangeDateRoomListStatusRequestDto requestDto) {
        List<String> dateRoomIdList = requestDto.getDateRoomIdList();
        log.info("CHANGING DATEROOM STATUS {}", dateRoomIdList.toString());
        long roomReservationState = requestDto.getRoomReservationState();
        List<DateRoom> dateRoomList = dateRoomRepository.findAllById(dateRoomIdList);
        if (dateRoomList.size()==0) return GeneralResponseDto.builder().success(false).message("유효한 dateroomId가 아닙니다.").build();
        for (DateRoom dateRoom:dateRoomList) {
            try {
                switch ((int) roomReservationState) {
                    case 0:
                        List<Reservation> reservations = dateRoom.getMapDateRoomReservations().stream().map(MapDateRoomReservation::getReservation).collect(Collectors.toList());
                        for (Reservation reservation:reservations) if (reservation.getReservationState() == 1) throw new RoomReservationException("예약되어 있는 날짜입니다.");
                        dateRoom.resetState();
                        break;
                    case 1:
                        dateRoom.setStateBooked();
                        break;
                }
            } catch (RoomReservationException roomReservationException) {
                return GeneralResponseDto.builder().success(false).message("해당 날짜의 예약 상태를 변경할 수 없습니다.").build();
            }
        }
        return GeneralResponseDto.builder().success(true).build();
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
