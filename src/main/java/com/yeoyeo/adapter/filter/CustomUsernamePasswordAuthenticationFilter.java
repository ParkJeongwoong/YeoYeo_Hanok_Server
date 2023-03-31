package com.yeoyeo.adapter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.admin.dto.LoginDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authenticationToken;

        if (request.getContentType().equals(MimeTypeUtils.APPLICATION_JSON_VALUE)) {
            try {
                LoginDto loginDto = objectMapper.readValue(request.getReader().lines().collect(Collectors.joining()), LoginDto.class);
                authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getUserId(), loginDto.getUserPassword());
            } catch (IOException e) {
                log.error("CustomUsernamePasswordAuthenticationFilter IOException", e);
                throw new AuthenticationServiceException("Request Content-Type(application/json) Parsing Error");
            }
        } else {
            String userId = obtainUsername(request);
            String userPassword = obtainPassword(request);
            authenticationToken = new UsernamePasswordAuthenticationToken(userId, userPassword);
        }
        this.setDetails(request, authenticationToken);
        return this.getAuthenticationManager().authenticate(authenticationToken);
    }

}
