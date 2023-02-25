package com.yeoyeo.application.sms.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SendMessageResponseDto {

    private String statusCode;
    private String statusName;
    private String requestId;
    private String requestTime;

    public SendMessageResponseDto(String statusCode, String statusName, String requestId, String requestTime) {
        this.statusCode = statusCode;
        this.statusName = statusName;
        this.requestId = requestId;
        this.requestTime = requestTime;
    }

}
