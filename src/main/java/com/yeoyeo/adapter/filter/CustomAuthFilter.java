package com.yeoyeo.adapter.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

@Slf4j
public class CustomAuthFilter extends RememberMeAuthenticationFilter {

	private AuthenticationManager authenticationManager;

	public CustomAuthFilter(
		AuthenticationManager authenticationManager, RememberMeServices rememberMeServices) {
		super(authenticationManager, rememberMeServices);
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		log.info("## CustomAuthFilter!!!");

		Authentication auth = getRememberMeServices().autoLogin((HttpServletRequest) request, (HttpServletResponse) response);
		log.info("auth1 : {}", auth);
		if (auth != null) {
			try {
				auth = this.authenticationManager.authenticate(auth);
				log.info("auth2 : {}", auth);
			} catch (AuthenticationException e) {
				log.error("AuthenticationException : {}", e);
			}
		}

		super.doFilter(request, response, chain);
	}

//	protected void doFilter(HttpServletRequest request, HttpServletResponse response,
//		FilterChain filterChain) throws ServletException, IOException {
//		log.info("## CustomAuthFilter!!!");
//		String ip = getIp(request);
//		String rememberMe = getCookie(request);
//		log.info("rememberMe : " + rememberMe);
//		filterChain.doFilter(request, response);
//	}
//
//	private String getCookie(HttpServletRequest request) {
//		String cookie = request.getHeader("Cookie");
//		log.info(">>>> Cookie : " + cookie);
//		if (cookie != null) {
//			String[] cookies = cookie.split(";");
//			for (String c : cookies) {
//				if (c.contains("remember-me")) {
//					return c;
//				}
//			}
//		}
//		return null;
//	}
//
//	private String getIp(HttpServletRequest request) {
//		String ip = request.getHeader("X-Forwarded-For");
//		log.info(">>>> X-FORWARDED-FOR : " + ip);
//		if (ip == null) {
//			ip = request.getHeader("Proxy-Client-IP");
//			log.info(">>>> Proxy-Client-IP : " + ip);
//		}
//		else if (ip == null) {
//			ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직
//			log.info(">>>> WL-Proxy-Client-IP : " + ip);
//		}
//		else if (ip == null) {
//			ip = request.getHeader("HTTP_CLIENT_IP");
//			log.info(">>>> HTTP_CLIENT_IP : " + ip);
//		}
//		else if (ip == null) {
//			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
//			log.info(">>>> HTTP_X_FORWARDED_FOR : " + ip);
//		}
//		else if (ip == null) {
//			ip = request.getRemoteAddr();
//		}
//		log.info(">>>> Result : IP Address : " + ip);
//		return ip;
//	}

}
