package com.yeoyeo.application.general.webclient;

import org.json.simple.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class WebClientService {

    public WebClient WebClient(String contentType) {
        return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, contentType).build();
    }

    public WebClient WebClient(String contentType, String baseUrl) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, contentType).uriBuilderFactory(factory).baseUrl(baseUrl).build();
    }

    public <T> T getWithAuth(String contentType, String url, String authKey, Class<T> t) {
        return WebClient(contentType).get()
                .uri(url)
                .header("Authorization", authKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<T>() {
                })
                .block();
    }

    public <T> T get(String contentType, String url, Class<T> t) {
        return WebClient(contentType).get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<T>() {
                })
                .block();
    }

    public JSONObject get(String contentType, String url) {
        return WebClient(contentType, url).get()
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();
    }

}
