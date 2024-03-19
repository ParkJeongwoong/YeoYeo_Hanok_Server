package com.yeoyeo.application.collision.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Reservation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CollisionHandleService {

	private final ReservationService reservationService;
	private final PaymentService paymentService;
	private final OfferService offerService;
	private final MessageService messageService;

	@Transactional
	public GeneralResponseDto collideRefund(Reservation reservation) {
		if (isChangeable(reservation)) {
			return offerChangeRoom(reservation);
		} else {
			return paymentService.refundBySystem(reservation);
		}
	}

	public GeneralResponseDto offerChangeRoom(Reservation reservation) {
		log.info("방 변경 제안 요청 - 예약번호 : {}", reservation.getId());
		try {
			reservationService.changeStateWait(reservation);
			offerService.expireOffer(reservation.getId()); // Async
			messageService.sendChangeOfferMsg(reservation);
			log.info("방 변경 제안 완료 (예약번호 : {})", reservation.getId());
			return GeneralResponseDto.builder().success(true).resultId(2).message("방 변경 제안이 완료되었습니다.").build();
		} catch (ReservationException e) {
			log.error("예약 상태 변경 실패", e);
			return GeneralResponseDto.builder().success(false).resultId(0).message(e.getMessage()).build();
		}
	}

	private boolean isChangeable(Reservation reservation) {
		List<DateRoom> anotherDateRoomList = reservationService.getAnotherRoomDateRoomList(reservation);
		return anotherDateRoomList.stream().allMatch(dateRoom -> dateRoom.getRoomReservationState() == 0);
	}

}
