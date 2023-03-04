package com.yeoyeo.application.general.webclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.sms.dto.SendMessageRequestDto;
import com.yeoyeo.application.sms.dto.SendMessageResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;

@Slf4j
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

    public WebClient smsWebClient(String timestamp, String accessKey, String signature) {
        return WebClient.builder()
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add("x-ncp-apigw-timestamp", timestamp);
                    headers.add("x-ncp-iam-access-key", accessKey);
                    headers.add("x-ncp-apigw-signature-v2", signature);
                })
                .build();
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

    public SendMessageResponseDto sendSms(String url, String subject, String content, String to, String timestamp, String accessKey, String signature) {
        SendMessageRequestDto requestDto = new SendMessageRequestDto(subject, content, to);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(requestDto);
            return smsWebClient(timestamp, accessKey, signature).post()
                    .uri(url)
                    .body(BodyInserters.fromValue(jsonString))
                    .retrieve()
                    .bodyToMono(SendMessageResponseDto.class)
                    .block();
        } catch (JsonProcessingException e) {
            log.error("requestDto JSON 변환 에러", e);
        }
        return null;
    }

    public SendMessageResponseDto sendMultipleSms(String url, String subject, String content, List<String> phoneNumberList, String timestamp, String accessKey, String signature) {
        SendMessageRequestDto requestDto = new SendMessageRequestDto(subject, content, phoneNumberList);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(requestDto);
            return smsWebClient(timestamp, accessKey, signature).post()
                    .uri(url)
                    .body(BodyInserters.fromValue(jsonString))
                    .retrieve()
                    .bodyToMono(SendMessageResponseDto.class)
                    .block();
        } catch (JsonProcessingException e) {
            log.error("requestDto JSON 변환 에러", e);
        }
        return null;
    }

}
