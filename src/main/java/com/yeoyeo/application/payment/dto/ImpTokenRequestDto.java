package com.yeoyeo.application.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ImpTokenRequestDto {
    private final String imp_key;
    private final String imp_secret;

    @Builder
    public ImpTokenRequestDto(String imp_key, String imp_secret) {
        this.imp_key = imp_key;
        this.imp_secret = imp_secret;
    }
}
