package com.yeoyeo.application.scraping.service;

import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.calendar.service.SyncService;
import com.yeoyeo.application.common.service.WebClientService;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.application.scraping.dto.ScrapingGetNaverRequestDto;
import com.yeoyeo.application.scraping.dto.ScrapingGetNaverResponseDto;
import com.yeoyeo.application.scraping.dto.ScrapingNaverBookingInfo;
import com.yeoyeo.application.scraping.dto.ScrapingPostNaverRequestDto;
import com.yeoyeo.application.scraping.dto.ScrapingPostNaverResponseDto;
import com.yeoyeo.domain.DateRoom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScrapingService {

	@Value("${scraping.server.url}")
	String SCRAPING_SERVER;

	@Value("${scraping.accessKey}")
	String accessKey;

	private final WebClientService webClientService;
	private final CalendarService calendarService;
	private final SyncService syncService;

	public String TestConnection() {
		return webClientService.getString("application/json;charset=UTF-8", SCRAPING_SERVER);
	}

	public ScrapingGetNaverResponseDto GetReservationFromNaver(ScrapingGetNaverRequestDto requestDto) {
		requestDto.setActivationKey(accessKey);
		JSONObject response = webClientService.post("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/out", requestDto);
		return new ScrapingGetNaverResponseDto(response);
	}

	public void SyncReservationFromNaver(ScrapingGetNaverRequestDto requestDto) {
		requestDto.setActivationKey(accessKey);
		JSONObject response = webClientService.post("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/out", requestDto);
		ScrapingGetNaverResponseDto responseDto = new ScrapingGetNaverResponseDto(response);
		List<ScrapingNaverBookingInfo> notCanceledBookingList = responseDto.getNotCanceledBookingList();
		calendarService.writeIcalendarFileByNaver(notCanceledBookingList);
	}

	public ScrapingPostNaverResponseDto PostReservationFromNaverAsync(ScrapingPostNaverRequestDto requestDto) {
		requestDto.setActivationKey(accessKey);
		JSONObject response = webClientService.post("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/in", requestDto);
		return new ScrapingPostNaverResponseDto(response);
	}

}
