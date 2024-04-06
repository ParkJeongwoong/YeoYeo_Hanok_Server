package com.yeoyeo.application.calendar.service;

import com.yeoyeo.application.collision.service.CollisionHandleService;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.Factory.GuestFactory;
import com.yeoyeo.domain.Guest.Guest;
import com.yeoyeo.domain.MapDateRoomReservation;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.component.VEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SyncService {

	private final ReservationService reservationService;
	private final CollisionHandleService collisionHandleService;
	private final MessageService messageService;

	private final DateRoomRepository dateRoomRepository;
	private final ReservationRepository reservationRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void asyncProcess(VEvent event, String uid, GuestFactory guestFactory, Payment payment, long roomId) {
		Reservation reservation = findExistingReservation(uid, roomId, guestFactory.getGuestClassName());
		if (reservation == null) registerReservation(event, guestFactory.createGuest(event.getDescription(), event.getSummary()), payment, roomId);
		else updateReservation(event, reservation);
	}

	private Reservation findExistingReservation(String uid, long roomId, String guestClassName) {
		List<Reservation> reservationList = reservationRepository.findAllByUniqueId(uid);
		log.info("COUNT : {}", reservationList.size());
		for (Reservation reservation : reservationList) {
			log.info("INFO : {} {} {}", reservation.getReservationState(), reservation.getRoom().getId(), reservation.getGuest().getName());
			if (reservation.getReservationState() == 5
				&& reservation.getRoom().getId() == roomId
				&& reservation.getReservedFrom().equals(guestClassName)) return reservation;
		}
		log.info("일치하는 reservation 없음");
		return null;
	}

	private void registerReservation(VEvent event, Guest guest, Payment payment, long roomId) {
		log.info("Reservation Sync - Register : {} / roomId : {} / uid : {}", guest.getName(), roomId, event.getUid().getValue());
		for (int i=0;i<3;i++) {
			try {
				String startDate = event.getStartDate().getValue();
				String endDate = event.getEndDate().getValue();
				log.info("Reservation between : {} ~ {}", startDate, endDate);
				if (checkExceedingAvailableDate(startDate, endDate)) {
					log.info("<예약 가능 기간 초과> - 예약 불가능한 날짜가 포함되어 있습니다.");
					return;
				}
				List<DateRoom> dateRoomList = getDateRoomList(startDate, endDate, roomId);
				if (dateRoomList != null && dateRoomList.size() > 0) {
					log.info("<등록 시작>");
					try {
						MakeReservationDto makeReservationDto = guest.createMakeReservationDto(
							dateRoomList, event.getDescription(), event.getSummary());
						Reservation reservation = reservationService.createReservation(
							makeReservationDto);
						reservation.setUniqueId(event.getUid().getValue());
						reservationService.setReservationPaid(reservation, payment);
						break;
					} catch (ReservationException reservationException) {
						log.info("[예약 충돌 발생] - 동기화 과정 중 중복된 예약 발생. 홈페이지 예약 변경/취소 작업 시작");
						Guest collidedGuest = checkUidChangeIssue(dateRoomList,
							guest.getName()); // UID 가 바뀌어서 기존 Guest 정보가 삭제되는 경우를 방지
						log.info("UID 변경 검증 결과 : {}", collidedGuest != null);
						if (collidedGuest != null)
							guest = collidedGuest; // UID가 바뀌었어도 예약 출처와 정보가 일치한다면 동일한 게스트로 간주
						boolean refundResult = collidedReservationCancel(dateRoomList);
						if (refundResult) {
							log.info("동기화 과정 중 중복된 예약 취소 완료. 재시도 시작");
						} else {
							log.info("동기화 과정 중 중복된 예약 취소 실패. 재시도 중지");
							messageService.sendAdminMsg(
								"동기화 중 중복된 예약 환불 미진행 (방변경 제안 / 시스템 장애) - 확인 필요. 체크인 날짜 : "
									+ startDate);
							break;
						}
					}
				} else
					break;
				if (i == 2) {
					log.info("동기화 중 중복된 예약 취소 실패. 재시도 횟수 초과");
					messageService.sendAdminMsg("동기화 오류 알림 - 중복된 예약을 취소하던 중 오류 발생");
				}
			} catch (Exception e) {
				log.error("동기화 예약 등록 중 예상치 못한 에러 발생", e);
				messageService.sendDevMsg("동기화 중 예약 등록 중 예상치 못한 에러 발생 - 확인 필요");
				break;
			}
		}
	}

	private void updateReservation(VEvent event, Reservation reservation) {
		log.info("Reservation Sync - Update : {} {}~{} / {}", reservation.getRoom().getName(),reservation.getFirstDate(), reservation.getLastDateRoom().getDate(), reservation.getUniqueId());
		String eventStart = event.getStartDate().getValue();
		String eventEnd = event.getEndDate().getValue();
		try {
			if (!getLocalDateFromString(eventStart).isEqual(reservation.getFirstDate())
				|| !getLocalDateFromString(eventEnd).isEqual(reservation.getLastDateRoom().getDate().plusDays(1))) {
				log.info("Update 중 날짜 변동사항 발견 - 예약취소 후 재등록 : {} ~ {} -> {} ~ {}", reservation.getFirstDate(), reservation.getLastDateRoom().getDate(), eventStart, eventEnd);
				reservationService.cancel(reservation);
				Guest guestClone = reservation.getGuest().clone();
				Payment paymentClone = reservation.getPayment().clone();
				registerReservation(event, guestClone, paymentClone, reservation.getRoom().getId());
			} else reservation.setStateSyncEnd(); // 동기화 완료
		} catch (ReservationException e) {
			messageService.sendAdminMsg("동기화 오류 알림 - 수정된 예약정보 반영을 위해 기존 예약 변경 중 오류 발생");
			log.error("달력 동기화 - 수정된 정보 반영 중 에러", e);
		}
	}

	private boolean collidedReservationCancel(List<DateRoom> dateRoomList) {
		for (DateRoom dateRoom : dateRoomList) {
			List<Reservation> reservationList = dateRoom.getMapDateRoomReservations().stream().map(MapDateRoomReservation::getReservation).collect(
				Collectors.toList());
			for (Reservation collidedReservation : reservationList) {
				if (collidedReservation.getReservationState() == 1 || collidedReservation.getReservationState() == 5) {
					String uid = collidedReservation.getUniqueId();
					log.info("{} 날짜의 {} 방 예약 취소 - 예약번호 : {} / {}", dateRoom.getDate(), dateRoom.getRoom().getName(), collidedReservation.getId(), uid);
					if (uid == null || getPlatformName(uid).equals("yeoyeo")) {
						log.info("홈페이지 예약 취소");
//                        GeneralResponseDto response = paymentService.refundBySystem(collidedReservation);
						GeneralResponseDto response = collisionHandleService.collideRefund(collidedReservation);
						if (!response.isSuccess()) return false;
					} else {
						log.info("플랫폼 예약 취소");
						try {
							reservationService.cancel(collidedReservation);
						} catch (ReservationException e) {
							log.error("플랫폼 예약 취소 작업 중 실패", e);
							messageService.sendAdminMsg("동기화 오류 알림 - UID가 다른 플랫폼 예약 취소 작업 중 오류 발생");
						}
					}
				}
			}
		}
		return true;
	}

	private Guest checkUidChangeIssue(List<DateRoom> dateRoomList, String guestName) {
		log.info("UID 변경 검증");
		dateRoomList.sort(Comparator.comparing(DateRoom::getDate));
		Reservation collidedReservation = dateRoomList.get(0)
			.getMapDateRoomReservations().stream().map(MapDateRoomReservation::getReservation)
			.filter(reservation -> reservation.getReservationState() == 1 || reservation.getReservationState() == 5)
			.findFirst().orElse(null);
		if (collidedReservation != null
			&& collidedReservation.getManagementLevel() > 0
			&& collidedReservation.getGuest().getName().equals(guestName)
			&& collidedReservation.getFirstDate().isEqual(dateRoomList.get(0).getDate())
			&& collidedReservation.getLastDateRoom().getDate().isEqual(dateRoomList.get(dateRoomList.size()-1).getDate())) {
			return collidedReservation.getGuest();
		}
		return null;
	}

	private boolean checkExceedingAvailableDate(String start, String end) {
		LocalDate startDate = getLocalDateFromString(start);
		LocalDate lastDate = getLocalDateFromString(end);
		LocalDate today = LocalDate.now();
		LocalDate aYearAfter = today.plusMonths(6);
		return !startDate.isAfter(today) || !lastDate.isBefore(aYearAfter); // 시작일이 과거~오늘 or 종료일이 1년뒤~미래라면 True => 내일 ~ 1년 뒤의 하루 전 이면 False
	}

	private List<DateRoom> getDateRoomList(String start, String end, long roomId) {
		LocalDate startDate = getLocalDateFromString(start);
		LocalDate endDate = getLocalDateFromString(end).minusDays(1);
		LocalDate now = LocalDate.now();
		if (endDate.isBefore(now)) return new ArrayList<>();
		return dateRoomRepository.findAllByDateBetweenAndRoom_Id(startDate, endDate, roomId);
	}

	private String getPlatformName(String uid) {
		String[] strings = uid.split("@");
		return strings[strings.length-1];
	}

	private LocalDate getLocalDateFromString(String date) {
		return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
	}

}
