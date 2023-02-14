package com.yeoyeo.application.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GeneralResponseDto {
    private final Boolean success;
    private final long resultId;
    private final String message;

    @Builder
    public GeneralResponseDto(boolean success, long resultId, String message) {
        this.success = success;
        this.resultId = resultId;
        this.message = message;
    }
}
