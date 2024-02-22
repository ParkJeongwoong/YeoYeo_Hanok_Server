package com.yeoyeo.application.dateroom.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.common.exception.AsyncApiException;
import com.yeoyeo.application.common.exception.NoResponseException;
import com.yeoyeo.application.common.service.WebClientService;
import com.yeoyeo.application.dateroom.dto.ChangeDateRoomListPriceRequestDto;
import com.yeoyeo.application.dateroom.dto.ChangeDateRoomListStatusRequestDto;
import com.yeoyeo.application.dateroom.dto.DateRoom2MonthDto;
import com.yeoyeo.application.dateroom.dto.DateRoomIdPriceInfoDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoByDateDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoDto;
import com.yeoyeo.application.dateroom.dto.DateRoomPriceInfoDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.dateroom.repository.HolidayRepository;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Holiday;
import com.yeoyeo.domain.MapDateRoomReservation;
import com.yeoyeo.domain.Reservation;
import com.yeoyeo.domain.Room;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DateRoomService {

    private final RoomRepository roomRepository;
    private final DateRoomRepository dateRoomRepository;
    private final HolidayRepository holidayRepository;

    private final MessageService messageService;
    private final WebClientService webClientService;
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${data.holiday.key}")
    String holidayKey;

    public List<DateRoomInfoByDateDto> showAllDateRooms() {
        List<DateRoom> dateRoomList =  dateRoomRepository.findAllByOrderByDateAscRoom_Id();
        return getDateRoomInfoList(dateRoomList);
    }

    public DateRoom2MonthDto show2MonthsDateRooms(int year, int month) {
        LocalDate now = LocalDate.now();
        int cachedYear = now.getYear();
        int cachedMonth = now.getMonthValue();
        if (year == cachedYear && month == cachedMonth) {
            return cachedDateRoomInfo();
        }
        LocalDate thisMonthStartDate = LocalDate.of(year, month, 1);
        LocalDate nextMonthStartDate = thisMonthStartDate.plusMonths(1);
        List<DateRoomInfoByDateDto> thisMonth = getDateRoomInfoListByDate(thisMonthStartDate);
        List<DateRoomInfoByDateDto> nextMonth = getDateRoomInfoListByDate(nextMonthStartDate);
        return new DateRoom2MonthDto(thisMonth, nextMonth);
    }

    private List<DateRoomInfoByDateDto> getDateRoomInfoListByDate(LocalDate startDate) {
        return getDateRoomInfoList(getMonthDateRooms(startDate));
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
        if (response == null) { throw new NoResponseException("Return 데이터 문제"); }
        try {
            response = (JSONObject) parser.parse(response.toString());
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
                Holiday holiday = new Holiday(date, name);
                holidayRepository.save(holiday);
            } else { // 2개 이상이면 배열로 응답됨
                JSONArray holidays = (JSONArray) items.get("item");
                for (Object jsonObject : holidays) {
                    String dateString = String.valueOf(((JSONObject) jsonObject).get("locdate"));
                    String name = String.valueOf(((JSONObject) jsonObject).get("dateName"));
                    LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    Holiday holiday = new Holiday(date, name);
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
        dateRoomList.forEach(dateRoom->{
            dateRoom.resetDefaultPriceType(holidayRepository);
            updateCache(dateRoom);
        });
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
        if (dateRoomList.isEmpty()) return GeneralResponseDto.builder().success(false).message("유효한 dateroomId가 아닙니다.").build();
        for (DateRoom dateRoom:dateRoomList) {
            if (priceType == 0 && price>0) dateRoom.changePrice(price);
            else dateRoom.changePriceType(priceType);
            updateCache(dateRoom);
        }
        return GeneralResponseDto.builder().success(true).build();
    }

    @Transactional
    public GeneralResponseDto changeDateRoomListStatus(ChangeDateRoomListStatusRequestDto requestDto) {
        List<String> dateRoomIdList = requestDto.getDateRoomIdList();
        log.info("CHANGING DATEROOM STATUS {}", dateRoomIdList.toString());
        long roomReservationState = requestDto.getRoomReservationState();
        List<DateRoom> dateRoomList = dateRoomRepository.findAllById(dateRoomIdList);
        if (dateRoomList.isEmpty()) return GeneralResponseDto.builder().success(false).message("유효한 dateroomId가 아닙니다.").build();
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
                    default:
                        break;
                }
                updateCache(dateRoom);
            } catch (RoomReservationException roomReservationException) {
                return GeneralResponseDto.builder().success(false).message("해당 날짜의 예약 상태를 변경할 수 없습니다.").build();
            }
        }
        return GeneralResponseDto.builder().success(true).build();
    }

    private List<DateRoomInfoByDateDto> getDateRoomInfoList(List<DateRoom> dateRoomList) {
        List<DateRoomInfoByDateDto> dateRoomInfoByDateDtos = new ArrayList<>();
        dateRoomList.forEach(dateRoom -> {
            if (dateRoomInfoByDateDtos.isEmpty()) {
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

    public DateRoom2MonthDto cachedDateRoomInfo() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        LocalDate next = now.plusMonths(1);
        List<DateRoomInfoByDateDto> thisMonth = getOrSetCachedDateRoomInfoList(year, month);
        List<DateRoomInfoByDateDto> nextMonth = getOrSetCachedDateRoomInfoList(next.getYear(), next.getMonthValue());
        return new DateRoom2MonthDto(thisMonth, nextMonth);
    }

    public List<DateRoomInfoByDateDto> getOrSetCachedDateRoomInfoList(int year, int month) {
        HashOperations<String, String, DateRoomInfoDto> hashOperations = redisTemplate.opsForHash();
        List<DateRoomInfoByDateDto> dateRoomInfoByDateDtos = new ArrayList<>();
        // 여유 방 데이터
        String key1 = getDateRoomCacheKey(year, month, 1);
        Map<String, DateRoomInfoDto> entries1 = hashOperations.entries(key1);
        if (entries1.isEmpty()) {
            log.info("CACHE MISS (여유 방 데이터)");
            return setCachedDateRoomInfoList(year, month);
        }

        // 여행 방 데이터
        String key2 = getDateRoomCacheKey(year, month, 2);
        Map<String, DateRoomInfoDto> entries2 = hashOperations.entries(key2);
        if (entries2.isEmpty()) {
            log.info("CACHE MISS (여행 방 데이터)");
            return setCachedDateRoomInfoList(year, month);
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            String dateStr = date.toString();
            DateRoomInfoByDateDto dateRoomInfoByDateDto = new DateRoomInfoByDateDto(date, entries1.get(dateStr));
            if (entries2.containsKey(dateStr)) dateRoomInfoByDateDto.addDateRoomInfo(entries2.get(dateStr));
            dateRoomInfoByDateDtos.add(dateRoomInfoByDateDto);
        }

        return dateRoomInfoByDateDtos;
    }

    private List<DateRoomInfoByDateDto> setCachedDateRoomInfoList(int year, int month) {
        HashOperations<String, String, DateRoomInfoDto> hashOperations = redisTemplate.opsForHash();
        LocalDate startDate = LocalDate.of(year, month, 1);
        List<DateRoomInfoByDateDto> dateRoomInfoByDateDtos = getDateRoomInfoListByDate(startDate);
        String key1 = getDateRoomCacheKey(year, month, 1);
        String key2 = getDateRoomCacheKey(year, month, 2);
        log.info("key1: {} year: {} month: {}", key1, year, month);
        log.info("key2: {} year: {} month: {}", key2, year, month);

        dateRoomInfoByDateDtos.forEach(dto -> {
            String dateStr = dto.getDate().toString();
            dto.getRooms().forEach(dateRoomInfoDto -> {
                if (dateRoomInfoDto.getRoomName().equals("여유")) {
                    hashOperations.put(key1, dateStr, dateRoomInfoDto);
                }
                else {
                    hashOperations.put(key2, dateStr, dateRoomInfoDto);
                }
            });
        });

        return dateRoomInfoByDateDtos;
    }

    @Retryable(retryFor = {AsyncApiException.class}, maxAttempts = 5, backoff = @Backoff(random = true, delay = 1000, maxDelay = 3000))
    @Async
    public void updateCache(DateRoom dateRoom) {
        try {
            HashOperations<String, String, DateRoomInfoDto> hashOperations = redisTemplate.opsForHash();
            String key = getDateRoomCacheKey(dateRoom.getDate().getYear(), dateRoom.getDate().getMonthValue(), dateRoom.getRoom().getId());
            String hashKey = dateRoom.getDate().toString();
            // 해당 캐시 데이터가 있으면 업데이트, 없으면 Skip
            log.info("캐시 업데이트 시도: {} {}", key, hashKey);
            if (Boolean.TRUE.equals(hashOperations.hasKey(key, hashKey))) {
                hashOperations.put(key, hashKey, new DateRoomInfoDto(dateRoom));
                log.info("캐시 업데이트: {} {}", key, hashKey);
            }
            else {
                log.info("캐시 업데이트 Skip: {} {}", key, hashKey);
            }
        } catch (Exception e) {
            log.error("Dateroom {} - 캐시 업데이트 실패: {}", dateRoom.getId(), e.getMessage());
            throw new AsyncApiException("캐시 업데이트 비동기 작업 실패 : Dateroom ID " + dateRoom.getId(), e);
        }
    }
    @Recover
    public void recover(AsyncApiException e) {
        log.error("캐시 업데이트 비동기 작업 실패 후 Recover: {}", e.getMessage());
        messageService.sendDevMsg("캐시 업데이트 비동기 작업 실패: " + e.getMessage());
    }

    private String getDateRoomCacheKey(int year, int month, long roomId) {
        return year + "-" + month + ":" + roomId;
    }

}
