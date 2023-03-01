package com.yeoyeo.application.reservation.etc.scheduler;

import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @Transactional
    @Scheduled(cron = "0 1 0 * * *")
    private void dailyReservationCompletion() {
        LocalDate today = LocalDate.now();
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        try {
            for (Reservation reservation : reservationList) {
                if (reservation.getFirstDateRoom().getDate().isBefore(today)) reservation.setStateComplete();
                else break;
            }
            reservationRepository.saveAll(reservationList);
        } catch (ReservationException reservationException) {
            log.error("예약 완료 처리 중 오류 발생", reservationException);
        }
    }

}
