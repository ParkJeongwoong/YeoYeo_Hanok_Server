package com.yeoyeo.application.reservation.etc.exception;

public class NotPayedReservationException extends RuntimeException {
    public NotPayedReservationException(String msg) {
        super(msg);
    }
}
