package com.yeoyeo.application.scraping.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ScrapingPostNaverRequestDto {

	private String targetDateStr;
	private String targetRoom;
	private String activationKey;

	@Builder
	public ScrapingPostNaverRequestDto(String targetDateStr, String targetRoom, String activationKey) {
		this.targetDateStr = targetDateStr;
		this.targetRoom = targetRoom;
		this.activationKey = activationKey;
	}

	public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}

}