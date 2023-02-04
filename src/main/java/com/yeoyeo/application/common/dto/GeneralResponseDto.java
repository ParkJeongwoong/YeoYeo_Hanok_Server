package com.yeoyeo.application.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GeneralResponseDto {
    private final String successYN;
    private final long resultId;
    private final String message;

    @Builder
    public GeneralResponseDto(String successYN, long resultId, String message) {
        this.successYN = successYN;
        this.resultId = resultId;
        this.message = message;
    }
}
