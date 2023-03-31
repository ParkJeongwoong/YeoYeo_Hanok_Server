package com.yeoyeo.application.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginDto {

    private String userId;
    private String userPassword;

    public LoginDto(String userId, String userPassword) {
        this.userId = userId;
        this.userPassword = userPassword;
    }

}
