package com.yeoyeo.adapter.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        System.out.println("LoginSuccess");
        super.onAuthenticationSuccess(request, response, authentication);
        addSameSiteCookieAttribute(response);
        Cookie cookie = new Cookie("TEST", "!@34;SameSite=None;secure;");
        response.addCookie(cookie);
        // Spring Context Holder에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        response.setHeader("TEST", "SameSite=None;secure;");
//        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
//        System.out.println(headers.size());
//        boolean firstHeader = true;
//        System.out.println("0");
//        // there can be multiple Set-Cookie attributes
//        for (String header : headers) {
//            System.out.println("1");
//            System.out.println(header);
//            if (firstHeader) {
//                response.setHeader(HttpHeaders.SET_COOKIE,
//                        String.format("%s; %s", header, "SameSite=None;secure;"));
//                firstHeader = false;
//                System.out.println("NEW0 : " + String.format("%s;%s", header, "SameSite=None;secure;"));
//                System.out.println("NEW : " + header);
////                continue;
//            }
//            response.addHeader(HttpHeaders.SET_COOKIE,
//                    String.format("%s;%s", header, "SameSite=None;secure;"));
//            System.out.println("NEW : " + header);
//        }
//
//
//        Collection<String> newheaders = response.getHeaders(HttpHeaders.SET_COOKIE);
//        System.out.println(headers.size());
//        System.out.println("2");
//        // there can be multiple Set-Cookie attributes
//        for (String header : newheaders) {
//            System.out.println("3");
//            System.out.println(header);
//        }
//        Cookie cookie = new Cookie("TEST", "!@34");
//        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void addSameSiteCookieAttribute(HttpServletResponse response) {
        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        for (String header : headers) {
            if (firstHeader) {
                response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));
                firstHeader = false;
                continue;
            }
            response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));
        }
    }

}