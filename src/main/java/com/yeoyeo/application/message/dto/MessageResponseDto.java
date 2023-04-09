package com.yeoyeo.application.message.dto;

import lombok.Getter;

@Getter
public class MessageResponseDto {

    private final String messageId;
    private final String requestTime;
    private final String contentType;
    private final String countryCode;
    private final String from;
    private final String to;

    public MessageResponseDto(String messageId, String requestTime, String contentType, String countryCode, String from, String to) {
        this.messageId = messageId;
        this.requestTime = requestTime;
        this.contentType = contentType;
        this.countryCode = countryCode;
        this.from = from;
        this.to = to;
    }

}
