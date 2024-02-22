package com.yeoyeo.application.common.exception;

public class AsyncApiException extends RuntimeException {

	public AsyncApiException(String message) {
		super(message);
	}

	public AsyncApiException(String message, Throwable cause) {
		super(message, cause);
	}

}
