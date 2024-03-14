package com.yeoyeo.application.collision.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
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

	private final PaymentService paymentService;
	private final OfferService offerService;
	private final MessageService messageService;

	private final DateRoomRepository dateRoomRepository;
	private final ReservationRepository reservationRepository;

	public GeneralResponseDto collideRefund(Reservation reservation) {
		if (isChangeable(reservation)) {
			return offerChangeRoom(reservation);
		} else {
			return paymentService.refundBySystem(reservation);
		}
	}

	@Transactional
	public GeneralResponseDto offerChangeRoom(Reservation reservation) {
		log.info("방 변경 제안 요청 - 예약번호 : {}", reservation.getId());
		try {
			reservation.setStateChangeWait();
			reservationRepository.save(reservation);
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
		long roomId = reservation.getDateRoomList().get(0).getRoom().getId();
		long anotherRoomId = roomId == 1 ? 2 : 1;
		List<DateRoom> anotherDateRoomList = dateRoomRepository.findAllByDateBetweenAndRoom_Id(reservation.getFirstDate(), reservation.getLastDateRoom().getDate(), anotherRoomId);
		return anotherDateRoomList.stream().allMatch(dateRoom -> dateRoom.getRoomReservationState() == 0);
	}

}
