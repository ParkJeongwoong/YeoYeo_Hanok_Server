package com.yeoyeo.application.common.exception;

public class WrongResponseException extends RuntimeException {

	public WrongResponseException(String message) {
		super(message);
	}

	public WrongResponseException(String message, Throwable cause) {
		super(message, cause);
	}

}
