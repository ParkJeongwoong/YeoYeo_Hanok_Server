package com.yeoyeo.config;

import com.yeoyeo.application.common.async.CustomAsyncExceptionHandler;
import com.yeoyeo.application.message.service.MessageService;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

	@Bean(name = "syncScheduleExecutor")
	public Executor syncScheduleExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5); // 기본 풀 사이즈
		executor.setMaxPoolSize(10); // 최대 풀 사이즈
		executor.setQueueCapacity(50); // 대기 큐 사이즈
		executor.setThreadNamePrefix("yeoyeo-async-thread-"); // 쓰레드 이름 접두사
		executor.initialize();
		return executor;
	}

	@Bean(name = "redisExecutor")
	public Executor redisExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20); // 기본 풀 사이즈
		executor.setMaxPoolSize(40); // 최대 풀 사이즈
		executor.setQueueCapacity(200); // 대기 큐 사이즈
		executor.setThreadNamePrefix("yeoyeo-redis-thread-"); // 쓰레드 이름 접두사
		executor.initialize();
		return executor;
	}

	@Bean(name = "offerExecutor")
	public Executor offerExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5); // 기본 풀 사이즈
		executor.setMaxPoolSize(10); // 최대 풀 사이즈
		executor.setQueueCapacity(50); // 대기 큐 사이즈
		executor.setThreadNamePrefix("yeoyeo-offer-thread-"); // 쓰레드 이름 접두사
		executor.initialize();
		return executor;
	}

	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new CustomAsyncExceptionHandler(messageService);
	}

}
