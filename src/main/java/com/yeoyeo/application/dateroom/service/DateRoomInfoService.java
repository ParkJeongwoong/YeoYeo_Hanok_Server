package com.yeoyeo.application.dateroom.service;

import com.yeoyeo.application.dateroom.dto.DateRoomInfoByDateDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.domain.DateRoom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DateRoomInfoService {

	private final DateRoomRepository dateRoomRepository;

	public List<DateRoomInfoByDateDto> getDateRoomInfoListByDate(LocalDate startDate) {
		return getDateRoomInfoList(getMonthDateRooms(startDate));
	}

	private List<DateRoom> getMonthDateRooms(LocalDate firstMonthDate) {
		LocalDate lastMonthDate = firstMonthDate.plusMonths(1).minusDays(1);
		return dateRoomRepository.findAllByDateBetweenOrderByDateAscRoom_Id(firstMonthDate, lastMonthDate);
	}

	public List<DateRoomInfoByDateDto> getDateRoomInfoList(List<DateRoom> dateRoomList) {
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

}
