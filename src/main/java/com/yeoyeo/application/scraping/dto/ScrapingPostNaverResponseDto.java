package com.yeoyeo.application.scraping.dto;

import lombok.Builder;
import lombok.Getter;
import org.json.simple.JSONObject;

@Getter
public class ScrapingPostNaverResponseDto {

	private final String message;

	@Builder
	public ScrapingPostNaverResponseDto(String message) {
		this.message = message;
	}

	public ScrapingPostNaverResponseDto(JSONObject response) {
		this.message = String.valueOf(response.get("message"));
	}

}
