package com.yeoyeo.application.reservation.service;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.reservation.dto.MakeReservationDto;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public long makeReservation(MakeReservationDto reservationDto) {
        try {
            Reservation reservation = createReservation(reservationDto);
            setDataPayed(reservation.getDateRoom(), reservation);
            saveReservationData(reservation);
            log.info("{} 고객님의 예약이 완료되었습니다.", reservationDto.getGuest().getName());
            return reservation.getId();
        } catch (ReservationException reservationException) {
            log.error("makeReservation 예외 발생", reservationException);
            throw reservationException;
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
    private void setDataPayed(DateRoom dateRoom, Reservation reservation) {
        try {
            reservation.setStatePayed();
            dateRoom.setStateBooked();
        } catch (RoomReservationException e) {
            log.error("예약된 날짜 에러 - {}", reservation.getGuest().getName(), e);
            throw new ReservationException(e.getMessage());
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException e) {
            log.error("예약된 날짜 에러(낙관적 락) - {}", reservation.getGuest().getName(), e);
            throw new ReservationException(e.getMessage());
        }
    }

    @Transactional
    private void saveReservationData(Reservation reservation) {
        reservationRepository.save(reservation);
    }

}
