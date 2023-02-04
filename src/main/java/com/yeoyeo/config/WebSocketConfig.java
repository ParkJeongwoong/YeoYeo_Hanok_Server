package com.yeoyeo.config;

import com.yeoyeo.adapter.handler.ReservationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ReservationWebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(webSocketHandler, "ws/reservation")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("chrome-extension://*", "ws://*", "http://*",
                        "chrome-extension://pfdhoblngboilpfeibdedpjgfnlcodoo") // For Test
//                .setAllowedOrigins("http://localhost:3000", "http://localhost:8080", "ws://localhost:8080",
//                        "http://yeoyeo.co.kr")
                .withSockJS()
                ;

    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

}

