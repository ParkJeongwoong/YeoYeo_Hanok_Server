package com.yeoyeo.adapter.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class WebAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        System.out.println("Not authenticated");
        // 방법1. 인증되지 않은 경우 페이지 이동 시 사용
        response.sendRedirect("/login.html");
        // 방법2. 인증되지 않은 경우 에러코드 반환 시 사용
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
