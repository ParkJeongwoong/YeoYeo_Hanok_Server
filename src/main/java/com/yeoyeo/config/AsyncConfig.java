package com.yeoyeo.config;

import com.yeoyeo.application.common.exception.CustomAsyncExceptionHandler;
import com.yeoyeo.application.message.service.MessageService;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@RequiredArgsConstructor
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

	@Autowired
	private final MessageService messageService;

	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10); // 기본 풀 사이즈
		executor.setMaxPoolSize(20); // 최대 풀 사이즈
		executor.setQueueCapacity(500); // 대기 큐 사이즈
		executor.setThreadNamePrefix("yeoyeo-async-thread-"); // 쓰레드 이름 접두사
		executor.initialize();
		return executor;
	}

	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new CustomAsyncExceptionHandler(messageService);
	}

}
