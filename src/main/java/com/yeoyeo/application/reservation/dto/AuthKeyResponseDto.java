package com.yeoyeo.application.reservation.dto;

import lombok.Getter;

@Getter
public class AuthKeyResponseDto {

	private final String messageStatusCode;
	private final String token;

	public AuthKeyResponseDto(String messageStatusCode, String token) {
		this.messageStatusCode = messageStatusCode;
		this.token = token;
	}

}
