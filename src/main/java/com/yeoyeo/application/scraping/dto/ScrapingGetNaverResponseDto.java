package com.yeoyeo.application.scraping.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

@Slf4j
@Getter
public class ScrapingGetNaverResponseDto {

	private final String message;
	private final List<ScrapingNaverBookingInfo> notCanceledBookingList;
	private final List<ScrapingNaverBookingInfo> allBookingList;

	@Builder
	public ScrapingGetNaverResponseDto(String message, List<ScrapingNaverBookingInfo> notCanceledBookingList, List<ScrapingNaverBookingInfo> allBookingList) {
		this.message = message;
		this.notCanceledBookingList = notCanceledBookingList;
		this.allBookingList = allBookingList;
	}

	public ScrapingGetNaverResponseDto(JSONObject response) throws Exception {
		try {
			this.message = String.valueOf(response.get("message"));
			log.info("message: " + message);

			List<ScrapingNaverBookingInfo> notCanceledBookingList = new ArrayList<>();
			List<ScrapingNaverBookingInfo> allBookingList = new ArrayList<>();

			List notCanceledBookingListJSON = (List) response.get("notCanceledBookingList");

			for (Object bookingInfo : notCanceledBookingListJSON) {
				LinkedHashMap<String, Object> bookingInfoMap = (LinkedHashMap<String, Object>) bookingInfo;
				notCanceledBookingList.add(new ScrapingNaverBookingInfo(bookingInfoMap));
			}
			log.info("notCanceledBookingList length: " + notCanceledBookingList.size());

			List allBookingListJSON = (List) response.get("allBookingList");
			for (Object bookingInfo : allBookingListJSON) {
				LinkedHashMap<String, Object> bookingInfoMap = (LinkedHashMap<String, Object>) bookingInfo;
				allBookingList.add(new ScrapingNaverBookingInfo(bookingInfoMap));
			}
			log.info("allBookingList length: " + allBookingList.size());

			this.notCanceledBookingList = notCanceledBookingList;
			this.allBookingList = allBookingList;
		} catch (Exception e) {
			log.error("ScrapingGetNaverResponseDto error", e);
			throw e;
		}
	}

}
