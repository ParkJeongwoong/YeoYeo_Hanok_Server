package com.yeoyeo.application.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeoyeo.application.message.dto.SendMessageRequestDto;
import com.yeoyeo.application.message.dto.SendMessageResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
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

    public String getString(String contentType, String url) {
        return WebClient(contentType, url).get()
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    public JSONObject get(String contentType, String url, MediaType acceptType) {
        return WebClient(contentType, url).get()
                .accept(acceptType)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();
    }

    public JSONObject post(String contentType, String url, Object bodyValue) {
        return WebClient(contentType, url).post()
            .bodyValue(bodyValue)
            .retrieve()
            .bodyToMono(JSONObject.class)
            .block();
    }

    public JSONObject postWithErrorMsg(String contentType, String url, Object bodyValue, String errorMsg) {
        return WebClient(contentType, url).post()
            .bodyValue(bodyValue)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> Mono.error(new Exception(errorMsg)))
            .bodyToMono(JSONObject.class)
            .block();
    }

    public JSONObject post(String contentType, String url, Object bodyValue, String headerName, String headerValue) {
        if (bodyValue == null) {
            return WebClient(contentType, url).post()
                .header(headerName, headerValue)
                .retrieve()
                .bodyToMono(JSONObject.class)
                .block();
        }
        return WebClient(contentType, url).post()
            .header(headerName, headerValue)
            .bodyValue(bodyValue)
            .retrieve()
            .bodyToMono(JSONObject.class)
            .block();
    }

    public SendMessageResponseDto sendMessage(String type, String url, String subject, String content, String to, String timestamp, String accessKey, String signature) {
        SendMessageRequestDto requestDto = new SendMessageRequestDto(type, subject, content, to);
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

    public SendMessageResponseDto sendMultipleMessage(String type, String url, String subject, String content, List<String> phoneNumberList, String timestamp, String accessKey, String signature) {
        SendMessageRequestDto requestDto = new SendMessageRequestDto(type, subject, content, phoneNumberList);
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
