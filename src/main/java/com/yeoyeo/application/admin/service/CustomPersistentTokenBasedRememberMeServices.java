package com.yeoyeo.application.admin.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

// POST 요청에는 getParameter가 Null이라 remember-me 체크를 못하는 문제를 해결하기 위해 사용
@Slf4j
public class CustomPersistentTokenBasedRememberMeServices extends PersistentTokenBasedRememberMeServices {

	public CustomPersistentTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
		super(key, userDetailsService, tokenRepository);
	}

	public void customLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
		log.info("isAuthenticated : {}, getDetails {}", successfulAuthentication.isAuthenticated(), successfulAuthentication.getDetails());
		log.info("key : {}", getKey());
		if (successfulAuthentication.isAuthenticated() && (Boolean) successfulAuthentication.getDetails()) {
			super.onLoginSuccess(request, response, successfulAuthentication);
		}
	}

	@Override
	protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) {
		log.info("processAutoLoginCookie");
		String presentedSeries = cookieTokens[0];
		String presentedToken = cookieTokens[1];
		log.info("presentedSeries : {}, presentedToken : {}", presentedSeries, presentedToken);
		return super.processAutoLoginCookie(cookieTokens, request, response);
	}

	@Override
	protected void cancelCookie(HttpServletRequest request, HttpServletResponse response) {
		log.info("cancelCookie");
//		super.cancelCookie(request, response);
	}

}