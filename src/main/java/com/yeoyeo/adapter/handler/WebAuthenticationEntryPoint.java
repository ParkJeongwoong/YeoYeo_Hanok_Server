package com.yeoyeo.adapter.handler;

import com.yeoyeo.application.common.method.CommonMethod;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CommonMethod commonMethod;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.info("Not authenticated");
        commonMethod.printIp("WebAuthenticationEntryPoint");
        // 방법1. 인증되지 않은 경우 페이지 이동 시 사용
//        response.sendRedirect("/login.html");
        // 방법2. 인증되지 않은 경우 에러코드 반환 시 사용
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

}
