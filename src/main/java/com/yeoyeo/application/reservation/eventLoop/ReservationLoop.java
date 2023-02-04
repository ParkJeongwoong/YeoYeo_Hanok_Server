package com.yeoyeo.application.reservation.eventLoop;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.ReservationSchedulerDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationLoop extends Thread {

    private static final long SLEEP_INTERVAL_BY_MINUTE = 1;

    private final NotPayedReservation notPayedReservation;
    private final ReservationRepository reservationRepository;
    private final DateRoomRepository dateRoomRepository;

    @Override
    public void run() {
        log.info("Reservation Loop 시작");
        log.info("Thread Cnt : {}", Thread.activeCount());
        while (true) {
            log.info("결제 대기 예약 수 : {}", notPayedReservation.countNotPayedReservation());
            try {
                if (notPayedReservation.getFirstReservation() != null) {
                    ReservationSchedulerDto reservationDto = notPayedReservation.getFirstReservation();
                    if (expirationCheck(reservationDto.getExpirationTime())) {
                        log.info("미결제 예약 취소 - 예약번호 : {}", reservationDto.getReservationId());
                        expireReservation(notPayedReservation.popFirstReservation().getReservationId());
                    } else {
                        Thread.sleep(getSleepTime(notPayedReservation.getFirstReservation().getExpirationTime())*1000);
                    }
                } else { // 대기 예약 없음 -> Sleep
                    Thread.sleep(SLEEP_INTERVAL_BY_MINUTE * 60000);
                }
            } catch (InterruptedException e) {
                log.info("Interrupted");
            }
        }
    }

    private boolean expirationCheck(LocalDateTime expirationTime) {
        return expirationTime.isBefore(LocalDateTime.now());
    }

    private void expireReservation(long reservationId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new);
            DateRoom dateRoom = reservation.getDateRoom();
            reservation.setStateTimeOut();
            dateRoom.resetState();
            reservationRepository.save(reservation);
            dateRoomRepository.save(dateRoom);
        } catch (NoSuchElementException e) {
            log.error("Reservation 조회 에러", e);
        } catch (ReservationException e) {
            log.info("결제가 완료된 예약입니다.");
        }
    }

    private long getSleepTime(LocalDateTime targetTime) {
        Duration duration = Duration.between(LocalDateTime.now(), targetTime);
        log.info("Sleep for {}s", duration.getSeconds()+1);
        return duration.getSeconds()+1;
    }

}
