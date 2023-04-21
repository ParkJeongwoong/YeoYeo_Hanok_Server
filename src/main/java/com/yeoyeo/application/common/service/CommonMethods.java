package com.yeoyeo.application.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class CommonMethods {

    public void printIp(String from) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getHeader("X-FORWARDED-FOR");
        log.info("from {} - X-FORWARDED-FOR : {}", from, ip);
        if (ip == null) {
            ip = request.getRemoteAddr();
            log.info("from {} - getRemoteAddr: {}", from, ip);
        }
    }

}
