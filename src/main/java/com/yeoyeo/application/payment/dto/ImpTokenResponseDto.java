package com.yeoyeo.application.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
@Getter
public class ImpTokenResponseDto {
    private long code;
    private String message;
    private HashMap<String, String> response;
//    private String access_token;
//    private long now;
//    private long expired_at;
}
