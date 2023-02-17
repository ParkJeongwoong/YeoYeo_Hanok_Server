package com.yeoyeo.application.payment.etc.exception;

public class WaitingWebhookException extends Exception {
    public WaitingWebhookException(String msg) {
        super(msg);
    }
}
