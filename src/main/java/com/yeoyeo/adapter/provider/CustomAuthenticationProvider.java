package com.yeoyeo.adapter.provider;

import com.yeoyeo.application.admin.service.CustomUserDetailsService;
import com.yeoyeo.domain.Admin.Administrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Slf4j
public class CustomAuthenticationProvider extends RememberMeAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailsService userDetailsService;

    public CustomAuthenticationProvider(String key, CustomUserDetailsService userDetailsService) {
        super(key);
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("[Auth Provider]");
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

        // AuthenticationFilter 에서 생성된 토큰으로 아이디와 비밀번호를 조회
        String userId = token.getName();
        String userPw = (String) token.getCredentials();

        log.info("ID : {}", userId);
        log.info("PW : {}", userPw);
        Administrator administrator = userDetailsService.loadUserById(userId);
        log.info("NAME : {}", administrator.getName());

        // 비밀번호 일치 여부 체크
        if (!administrator.checkPassword(userPw)) {
            throw new BadCredentialsException(userId + " Invalid password");
        }
        log.info("성공");

        // principal(접근대상 정보), credential(비밀번호), authorities(권한 목록)를 token에 담아 반환
        return new UsernamePasswordAuthenticationToken(administrator, userPw, administrator.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
