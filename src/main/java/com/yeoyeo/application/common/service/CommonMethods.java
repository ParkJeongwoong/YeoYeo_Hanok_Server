package com.yeoyeo.application.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class CommonMethods {

    public void printIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getHeader("X-FORWARDED-FOR");
        log.info("X-FORWARDED-FOR : {}", ip);
        if (ip == null) {
            ip = request.getRemoteAddr();
            log.info("getRemoteAddr: {}", ip);
        }
    }

}
