package com.yeoyeo.adapter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/*
    Spring Security는 API 방식(JSON 이 담긴 Request Body)를 이용한 인증을 지원하지 않기 때문에 API용 Filter가 필요 (이게 없으면 로그인 요청이 GET 요청으로만 받을 수 있음)
 */

@Slf4j
public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/admin/login", "POST");

    public ApiAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        System.out.println("API Filter");
        if (!isValidRequest(request)) throw new AuthenticationServiceException("로그인 요청 Method, Type이 잘못되었습다.");

        Map<String, Object> parsedJsonMap = parseJsonMap(request);

        String userId = (String) parsedJsonMap.get("userId");
        String userPassword = (String) parsedJsonMap.get("userPassword");
        Boolean remember = (Boolean) parsedJsonMap.get("remember");
        log.info("{} {} {}",userId, userPassword, remember);

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userId, userPassword);
        authRequest.setDetails(remember);

        return super.getAuthenticationManager().authenticate(authRequest);
    }

    private Boolean isValidRequest(HttpServletRequest request) {
        if (!request.getMethod().equals(HttpMethod.POST.name())) return false;
        if (!request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE)) return false;
        return true;
    }

    private Map<String, Object> parseJsonMap(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining());
        log.info(body);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(body, Map.class);
    }

}
