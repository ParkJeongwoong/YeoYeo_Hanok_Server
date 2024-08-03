package com.yeoyeo.application.scraping.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ScrapingGetNaverRequestDto {

	private int monthSize;
	private String activationKey;

	@Builder
	public ScrapingGetNaverRequestDto(int monthSize) {
		this.monthSize = monthSize;
	}

	public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}

}
