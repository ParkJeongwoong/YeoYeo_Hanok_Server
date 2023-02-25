package com.yeoyeo.application.sms.dto;

import lombok.Getter;

@Getter
public class FileDto {

    private final String name;
    private final String body;

    public FileDto(String name, String body) {
        this.name = name;
        this.body = body;
    }

}
