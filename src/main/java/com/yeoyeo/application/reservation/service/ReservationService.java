package com.yeoyeo.application.reservation.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.reservation.dto.MakeReservationDto;
import com.yeoyeo.application.reservation.dto.ReservationDetailInfoDto;
import com.yeoyeo.application.reservation.dto.ReservationInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public List<ReservationInfoDto> showReservations(int type) {
        switch (type) {
            // 현재 가상계좌 결제를 사용하지 않아 미결제 상태 0이 없음
            case 0: // 전체
                return reservationRepository.findAllByOrderByDateRoom_Date().stream().map(ReservationInfoDto::new).collect(Collectors.toList());
            default: // 1 : 숙박 대기, 2 : 숙박 완료, 3 : 예약 취소, 4 : 환불 완료
                return reservationRepository.findAllByReservationStateOrderByDateRoom_Date(type).stream().map(ReservationInfoDto::new).collect(Collectors.toList());
        }
    }

    public ReservationDetailInfoDto getReservationInfo(long reservationId) throws ReservationException {
        return new ReservationDetailInfoDto(reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new));
    }

    @Transactional
    public long makeReservation(MakeReservationDto reservationDto) throws ReservationException {
        try {
            Reservation reservation = createReservation(reservationDto);
            setDataPaid(reservation.getDateRoom(), reservation);
            reservationRepository.save(reservation);
            log.info("{} 고객님의 예약이 완료되었습니다.", reservationDto.getGuest().getName());
            return reservation.getId();
        } catch (ReservationException reservationException) {
            log.error("makeReservation 예외 발생", reservationException);
            throw reservationException;
        }
    }

    @Transactional
    public GeneralResponseDto cancel(long reservationId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new);
            DateRoom dateRoom = reservation.getDateRoom();
            reservation.setStateCanceled();
            dateRoom.resetState();
            reservationRepository.save(reservation);
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
        } catch (RoomReservationException roomReservationException) {
            log.error("Dateroom 상태 변경 에러", roomReservationException);
            return GeneralResponseDto.builder()
                    .success(false)
                    .message(roomReservationException.getMessage())
                    .build();
        }
    }

    @Transactional
    public void cancel(DateRoom dateRoom, Reservation reservation) throws ReservationException {
        try {
            reservation.setStateRefund();
            dateRoom.resetState();
            reservationRepository.save(reservation);
            log.info("{} 고객님의 예약이 취소되었습니다.", reservation.getGuest().getName());
        } catch (ReservationException reservationException) {
            log.error("Reservation 상태 변경 에러", reservationException);
            throw new ReservationException(reservationException.getMessage());
        } catch (RoomReservationException roomReservationException) {
            log.error("Dateroom 상태 변경 에러", roomReservationException);
            throw new ReservationException(roomReservationException.getMessage());
        }
    }

    private Reservation createReservation(MakeReservationDto reservationDto) {
        DateRoom dateRoom = reservationDto.getDateRoom();
        Guest guest = reservationDto.getGuest();
        Payment payment = reservationDto.getPayment();
        return Reservation.builder()
                .dateRoom(dateRoom)
                .guest(guest)
                .payment(payment)
                .build();
    }

    @Transactional
    private void setDataPaid(DateRoom dateRoom, Reservation reservation) throws ReservationException {
        try {
            reservation.setStatePaid();
            dateRoom.setStateBooked();
        } catch (RoomReservationException e) {
            log.error("예약된 날짜 에러 - {}", reservation.getGuest().getName(), e);
            throw new ReservationException(e.getMessage());
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException e) {
            log.error("예약된 날짜 에러(낙관적 락) - {}", reservation.getGuest().getName(), e);
            throw new ReservationException(e.getMessage());
        } catch (ReservationException e) {
            e.printStackTrace();
        }
    }

}
