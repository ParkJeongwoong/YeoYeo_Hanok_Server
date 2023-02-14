package com.yeoyeo.application.general.webclient;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class WebClientService {

    public WebClient WebClient(String contentType) {
        return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, contentType).build();
    }

}
