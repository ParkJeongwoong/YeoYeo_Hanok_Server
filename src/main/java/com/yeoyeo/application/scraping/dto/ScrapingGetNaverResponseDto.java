package com.yeoyeo.application.scraping.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

	public ScrapingGetNaverResponseDto(JSONObject response) {
		this.message = String.valueOf(response.get("message"));

		List<ScrapingNaverBookingInfo> notCanceledBookingList = new ArrayList<>();
		List<ScrapingNaverBookingInfo> allBookingList = new ArrayList<>();

		JSONArray notCanceledBookingListJSON = (JSONArray) response.get("notCanceledBookingList");
		for (Object bookingInfo : notCanceledBookingListJSON) {
			JSONObject bookingInfoJSON = (JSONObject) bookingInfo;
			notCanceledBookingList.add(new ScrapingNaverBookingInfo(bookingInfoJSON));
		}

		JSONArray allBookingListJSON = (JSONArray) response.get("allBookingList");
		for (Object bookingInfo : allBookingListJSON) {
			JSONObject bookingInfoJSON = (JSONObject) bookingInfo;
			allBookingList.add(new ScrapingNaverBookingInfo(bookingInfoJSON));
		}

		this.notCanceledBookingList = notCanceledBookingList;
		this.allBookingList = allBookingList;
	}

}
