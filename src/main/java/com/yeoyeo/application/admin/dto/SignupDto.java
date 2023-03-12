package com.yeoyeo.application.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupDto {

    private String userId;
    private String userPassword;
    private String userName;
    private String userContact;

    public SignupDto(String userId, String userPassword, String userName, String userContact) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userContact = userContact;
    }

}
