package com.yeoyeo.application.common.exception;

import com.yeoyeo.application.message.service.MessageService;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Slf4j
@RequiredArgsConstructor
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	private final MessageService messageService;


	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		log.error("Exception message - " + ex.getMessage());
		log.error("Method name - " + method.getName());
		for (Object param : params) {
			log.error("Parameter value - " + param);
		}
		messageService.sendDevMsg(ex.getMessage());
	}

}
