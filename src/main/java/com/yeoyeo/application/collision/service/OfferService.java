package com.yeoyeo.application.collision.service;

import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.Reservation;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OfferService {

	private final ReservationService reservationService;
	private final PaymentService paymentService;

	private final ReservationRepository reservationRepository;

	@Async("offerExecutor")
	@Transactional
	public void expireOffer(Long reservationId) {
		try {
			reservationService.setThreadName(reservationId, Thread.currentThread().getName());
			Thread.sleep(6 * 60 * 60 * 1000);
		} catch (InterruptedException e) {
			log.info("예약 변경 제안에 대한 응답 수신");
		}
		reservationService.removeThreadName(reservationId);
		Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
			NoSuchElementException::new);
		if (reservation.getReservationState() == 3) {
			paymentService.refundBySystem(reservation);
		}
	}

}
