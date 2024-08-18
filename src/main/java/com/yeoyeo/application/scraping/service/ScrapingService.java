package com.yeoyeo.application.scraping.service;

import com.yeoyeo.application.calendar.service.CalendarService;
import com.yeoyeo.application.common.service.WebClientService;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.scraping.dto.ScrapingGetNaverRequestDto;
import com.yeoyeo.application.scraping.dto.ScrapingGetNaverResponseDto;
import com.yeoyeo.application.scraping.dto.ScrapingNaverBookingInfo;
import com.yeoyeo.application.scraping.dto.ScrapingPostNaverRequestDto;
import com.yeoyeo.application.scraping.dto.ScrapingPostNaverResponseDto;
import com.yeoyeo.domain.Reservation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;

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
	private final MessageService messageService;

	public String TestConnection() {
		return webClientService.getString("application/json;charset=UTF-8", SCRAPING_SERVER);
	}

	public ScrapingGetNaverResponseDto GetReservationFromNaver(ScrapingGetNaverRequestDto requestDto) {
		requestDto.setActivationKey(accessKey);
		JSONObject response = webClientService.post("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/out", requestDto);
		return new ScrapingGetNaverResponseDto(response);
	}

	public ScrapingPostNaverResponseDto PostReservationFromNaverAsync(ScrapingPostNaverRequestDto requestDto) {
		requestDto.setActivationKey(accessKey);
		JSONObject response = webClientService.post("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/in", requestDto);
		return new ScrapingPostNaverResponseDto(response);
	}

	@Transactional
	public void SyncReservationFromNaver(ScrapingGetNaverRequestDto requestDto) {
		requestDto.setActivationKey(accessKey);
		ClientResponse response = webClientService.postWithClientResponse("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/out", requestDto);
		int statusCode = response.statusCode().value();
		if (statusCode != 200) {
			messageService.sendDevMsg("네이버->서버 동기화 실패");
			return;
		}
		JSONObject jsonObject = response.bodyToMono(JSONObject.class).block();
		ScrapingGetNaverResponseDto responseDto = new ScrapingGetNaverResponseDto(jsonObject);
		List<ScrapingNaverBookingInfo> notCanceledBookingList = responseDto.getNotCanceledBookingList();
		for (ScrapingNaverBookingInfo bookingInfo : notCanceledBookingList) {
			log.info("bookingInfo : {} ({} ~ {})", bookingInfo.getName(), bookingInfo.getStartDate(), bookingInfo.getEndDate());
		}
		calendarService.writeIcalendarFileByNaver(notCanceledBookingList);
	}

	public void SyncReservationToNaver(ScrapingPostNaverRequestDto requestDto) {
		Reservation reservation = new Reservation();
		// TODO : 특정 CreatedAt 이후로 동기화
	}

}
