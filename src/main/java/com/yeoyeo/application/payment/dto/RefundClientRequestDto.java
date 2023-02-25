package com.yeoyeo.application.payment.dto;

import com.yeoyeo.application.payment.etc.exception.PaymentException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.Reservation;
import lombok.Getter;

import java.util.NoSuchElementException;

@Getter
public class RefundClientRequestDto {
    private long reservationId;
    private String phoneNumber;
    private String reason;

    public Reservation getValidatedReservation(ReservationRepository reservationRepository) throws PaymentException {
        Reservation reservation = reservationRepository.findById(this.reservationId).orElseThrow(NoSuchElementException::new);
        if (!this.phoneNumber.equals(reservation.getGuest().getPhoneNumber())) throw new PaymentException("휴대폰 번호가 일치하지 않습니다.");
        return reservation;
    }
}
