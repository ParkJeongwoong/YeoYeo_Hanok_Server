package com.yeoyeo.application.reservation.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationDto;
import com.yeoyeo.application.reservation.dto.ReservationDetailInfoDto;
import com.yeoyeo.application.reservation.dto.ReservationInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.sms.service.SmsService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SmsService smsService;

    public List<ReservationInfoDto> showReservations(int type) {
        // 현재 가상계좌 결제를 사용하지 않아 미결제 상태 0이 없음

        if (type == 0) { // 전체
            return reservationRepository.findAll().stream().sorted(Comparator.comparing(Reservation::getFirstDate)).map(ReservationInfoDto::new).collect(Collectors.toList());
        } else { // 1 : 숙박 대기, 2 : 숙박 완료, 3 : 예약 취소, 4 : 환불 완료
            return reservationRepository.findAllByReservationState(type).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).map(ReservationInfoDto::new).collect(Collectors.toList());
        }
    }

    public ReservationDetailInfoDto getReservationInfo(long reservationId) throws ReservationException {
        return new ReservationDetailInfoDto(reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new));
    }

    @Transactional
    public long createReservation(MakeReservationDto reservationDto) throws ReservationException {
        List<DateRoom> dateRoomList = reservationDto.getDateRoomList();
        for (DateRoom dateRoom:dateRoomList) if (dateRoom.getRoomReservationState() != 0) throw new ReservationException("이미 예약이 완료된 방입니다.");
        Guest guest = reservationDto.getGuest();
        Reservation reservation =  Reservation.builder()
                .dateRoomList(dateRoomList)
                .guest(guest)
                .build();
        reservationRepository.save(reservation);
        log.info("{} 고객님의 예약 정보가 생성되었습니다.", reservationDto.getGuest().getName());
        return reservation.getId();
    }

    @Transactional
    public void setReservationPaid(Reservation reservation, Payment payment) throws ReservationException {
        List<DateRoom> dateRoomList = reservation.getDateRoomList();
        try {
            for (DateRoom dateRoom:dateRoomList) {
                log.info("{} : {} {} 예약시도", reservation.getGuest().getName(), dateRoom.getDate(), dateRoom.getRoom().getId());
                dateRoom.setStateBooked();
                log.info("{} : {} {} 예약성공", reservation.getGuest().getName(), dateRoom.getDate(), dateRoom.getRoom().getId());
            }
            log.info("예약 함 !! {}", reservation.getGuest().getName());
            reservation.setPayment(payment);
            reservationRepository.save(reservation);
        } catch (RoomReservationException e) {
            reservation.setStateCanceled();
            reservationRepository.save(reservation);
            log.error("이미 예약된 날짜입니다.", e);
            throw new ReservationException(e.getMessage());
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException e) {
            log.error("예약된 날짜 에러(낙관적 락) - {}", reservation.getGuest().getName(), e);
            throw new ReservationException(e.getMessage());
        } catch (ReservationException e) {
            log.error("데이터 변경 중 에러", e);
        }
    }

    @Transactional
    public GeneralResponseDto cancel(long reservationId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new);
            List<DateRoom> dateRoomList = reservation.getDateRoomList();
            reservation.setStateCanceled();
            for (DateRoom dateRoom:dateRoomList) dateRoom.resetState();
            reservationRepository.save(reservation);
            smsService.sendCancelSms(reservation);
            log.info("{} 고객님의 예약이 취소되었습니다.", reservation.getGuest().getName());
            return GeneralResponseDto.builder()
                    .success(true)
                    .message("예약이 취소되었습니다.")
                    .build();
        } catch (ReservationException reservationException) {
            log.error("Reservation 상태 변경 에러", reservationException);
            return GeneralResponseDto.builder()
                    .success(false)
                    .message(reservationException.getMessage())
                    .build();
        } catch (RoomReservationException e) {
            log.error("Dateroom 상태 변경 에러", e);
            return GeneralResponseDto.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void cancel(Reservation reservation) throws ReservationException {
        try {
            reservation.setStateRefund();
            for (DateRoom dateRoom:reservation.getDateRoomList()) dateRoom.resetState();
            reservationRepository.save(reservation);
            log.info("{} 고객님의 예약이 취소되었습니다.", reservation.getGuest().getName());
        } catch (ReservationException reservationException) {
            log.error("Reservation 상태 변경 에러", reservationException);
            throw new ReservationException(reservationException.getMessage());
        } catch (RoomReservationException e) {
            log.error("Dateroom 상태 변경 에러", e);
            throw new ReservationException(e.getMessage());
        }
    }

}
