package com.yeoyeo.application.reservation.eventLoop;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.ReservationSchedulerDto;
import com.yeoyeo.application.reservation.etc.exception.NotPayedReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationHandler  {

    private static final long EXPIRATION_TIME = 1;

    private final NotPayedReservation notPayedReservation;
    private final ReservationRepository reservationRepository;
    private final DateRoomRepository dateRoomRepository;
    private ReservationLoop reservationLoop;

    @PostConstruct
    private void run() {
//        this.reservationLoop = new ReservationLoop(notPayedReservation,reservationRepository,dateRoomRepository);
//        reservationLoop.start();
    }

    public void add(long reservationId) {
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(EXPIRATION_TIME);
        ReservationSchedulerDto schedulerDto = ReservationSchedulerDto.builder()
                .reservationId(reservationId).expirationTime(expirationTime).build();
        if (!this.notPayedReservation.add(schedulerDto)) {
            log.error("미결제 예약 대기열 추가 실패", new NotPayedReservationException("미결제 예약 대기열 추가 실패"));
        }
        log.info("예약번호 : {} / 예약기한 : {}", reservationId, expirationTime);
        this.reservationLoop.interrupt();
    }

}
