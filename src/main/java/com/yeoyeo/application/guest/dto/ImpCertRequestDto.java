package com.yeoyeo.application.guest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ImpCertRequestDto {
    private final String imp_uid;

    @Builder
    public ImpCertRequestDto(String imp_uid) {
        this.imp_uid = imp_uid;
    }
}
