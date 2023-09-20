package com.yeoyeo.application.admin.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// POST 요청에는 getParameter가 Null이라 remember-me 체크를 못하는 문제를 해결하기 위해 사용
public class CustomPersistentTokenBasedRememberMeServices extends PersistentTokenBasedRememberMeServices {

    public CustomPersistentTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
    }

    public void customLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        if (successfulAuthentication.isAuthenticated() && (Boolean) successfulAuthentication.getDetails()) {
            super.onLoginSuccess(request, response, successfulAuthentication);
        }
    }
    
}
