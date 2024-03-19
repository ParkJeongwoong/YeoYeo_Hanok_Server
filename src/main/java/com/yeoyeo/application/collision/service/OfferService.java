package com.yeoyeo.application.collision.service;

import com.yeoyeo.application.dateroom.dto.DateRoomCacheDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.service.DateRoomCacheService;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Reservation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private final PaymentService paymentService;

	private final ReservationRepository reservationRepository;
	private final DateRoomCacheService dateRoomCacheService;

	Map<Long, String> changeOfferMap = new HashMap<>();

	@Async("offerExecutor")
	@Transactional
	public void expireOffer(Long reservationId) {
		try {
			setThreadName(reservationId, Thread.currentThread().getName());
			long expireTime = 6 * 60 * 60 * (long) 1000; // 6시간
			Thread.sleep(expireTime);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			log.info("비동기 스레드 대기 종료 - 예약번호 : {}", reservationId);
			boolean interrupted = Thread.interrupted();
			if (interrupted) {
				log.info("예약 변경 제안에 대한 응답 수신 - 예약번호 : {}", reservationId);
			} else {
				log.info("예약 변경 제안 만료 - 예약번호 : {}", reservationId);
			}
			removeThreadName(reservationId);
			Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new);
			if (reservation.getReservationState() == 3) {
				log.info("환불 작업 진행");
				paymentService.refundBySystem(reservation);
			}
		}
	}

	public void interruptOfferThread(long reservationId) {
		log.info("종료할 스레드의 예약번호 : {}", reservationId);
		String threadName = getThreadName(reservationId);
		if (threadName != null) {
			Thread.getAllStackTraces().keySet().stream()
				.filter(t -> t.getName().equals(threadName))
				.findFirst().ifPresent(Thread::interrupt);
			removeThreadName(reservationId);
		}
	}

	@Transactional
	public void acceptOffer(long reservationId) throws ReservationException {
		try {
			log.info("예약 변경 수락 - 예약 변경 작업 진행");
			Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new);
			reservation.setStatePaid();
			List<DateRoom> dateRoomList = reservation.getDateRoomList();
			for (DateRoom dateRoom : dateRoomList) {
				dateRoom.setStateBooked();
				dateRoomCacheService.updateCache(new DateRoomCacheDto(dateRoom));
			}
			reservationRepository.save(reservation);
		} catch (RoomReservationException e) {
			log.error("예약 변경 수락 실패 - {}", e.getMessage());
			throw new ReservationException(e.getMessage());
		}
	}

	public List<Long> getReservationIdList() {
		return new ArrayList<>(this.changeOfferMap.keySet());
	}

	private void setThreadName(Long reservationId, String threadName) {
		this.changeOfferMap.put(reservationId, threadName);
	}

	private String getThreadName(Long reservationId) {
		return this.changeOfferMap.get(reservationId);
	}

	private void removeThreadName(Long reservationId) {
		this.changeOfferMap.remove(reservationId);
	}

}
