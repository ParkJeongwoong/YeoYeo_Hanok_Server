package com.yeoyeo.application.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GeneralRequestDto {
    private final String key;
    private final String value;

    @Builder
    public GeneralRequestDto(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
