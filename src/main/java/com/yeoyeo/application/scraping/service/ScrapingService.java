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
		try {
			requestDto.setActivationKey(accessKey);
			JSONObject response = webClientService.post("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/out", requestDto);
		return new ScrapingGetNaverResponseDto(response);
		} catch (Exception e) {
			messageService.sendDevMsg("네이버->서버 동기화 실패");
			return null;
		}
	}

	public ScrapingPostNaverResponseDto PostReservationFromNaverAsync(ScrapingPostNaverRequestDto requestDto) {
		requestDto.setActivationKey(accessKey);
		JSONObject response = webClientService.post("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/in", requestDto);
		return new ScrapingPostNaverResponseDto(response);
	}

	@Transactional
	public void SyncReservationFromNaver(ScrapingGetNaverRequestDto requestDto) {
		try {
			requestDto.setActivationKey(accessKey);
			JSONObject response = webClientService.postWithErrorMsg("application/json;charset=UTF-8", SCRAPING_SERVER + "/sync/out", requestDto, "네이버->서버 동기화 실패");
			if (response == null) {
				messageService.sendDevMsg("네이버->서버 동기화 실패");
				return;
			}
			ScrapingGetNaverResponseDto responseDto = new ScrapingGetNaverResponseDto(response);
			List<ScrapingNaverBookingInfo> notCanceledBookingList = responseDto.getNotCanceledBookingList();
			for (ScrapingNaverBookingInfo bookingInfo : notCanceledBookingList) {
				log.info("bookingInfo : {} ({} ~ {})", bookingInfo.getName(), bookingInfo.getStartDate(), bookingInfo.getEndDate());
			}
			calendarService.writeIcalendarFileByNaver(notCanceledBookingList);
		} catch (Exception e) {
			messageService.sendDevMsg("네이버->서버 동기화 실패");
		}
	}

	public void SyncReservationToNaver(ScrapingPostNaverRequestDto requestDto) {
		Reservation reservation = new Reservation();
		// TODO : 특정 CreatedAt 이후로 동기화
	}

}
