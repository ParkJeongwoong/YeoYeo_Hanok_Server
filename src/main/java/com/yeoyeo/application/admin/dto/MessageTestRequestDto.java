package com.yeoyeo.application.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageTestRequestDto {
    private String phoneNumber;
    private String roomName;

    public MessageTestRequestDto(String phoneNumber, String roomName) {
        this.phoneNumber = phoneNumber;
        this.roomName = roomName;
    }
    public String getNumberOnlyPhoneNumber() {
        return this.phoneNumber.replaceAll("[^0-9]","");
    }
}
