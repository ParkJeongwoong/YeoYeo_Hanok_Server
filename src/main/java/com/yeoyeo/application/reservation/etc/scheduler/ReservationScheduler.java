package com.yeoyeo.application.reservation.etc.scheduler;

import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @PostConstruct
    private void init() {
        dailyReservationCompletion();
    }

    @Transactional
    @Scheduled(cron = "0 1 0 * * *")
    protected void dailyReservationCompletion() {
        LocalDate today = LocalDate.now();
        log.info("{} 예약 완료 처리 시작", today);
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        log.info("대기 중인 예약 건수 : {}건", reservationList.size());
        try {
            for (Reservation reservation : reservationList) {
                if (reservation.getFirstDateRoom().getDate().isBefore(today)) reservation.setStateComplete();
                else break;
            }
            reservationRepository.saveAll(reservationList);
        } catch (ReservationException reservationException) {
            log.error("예약 완료 처리 중 오류 발생", reservationException);
        }
        log.info("예약 완료 처리 정상 종료");
    }

    @Transactional
    @Scheduled(cron = "0 1 3 * * *")
    public void dailyReservationClearing() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("{} 일자 미결제 예약 삭제 처리 시작", yesterday);
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(0).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        log.info("삭제 예정 미결제 예약 건수 : {}건", reservationList.size());
        for (Reservation reservation : reservationList) {
            if (reservation.getFirstDateRoom()==null) reservationRepository.delete(reservation);
            else if (reservation.getFirstDateRoom().getDate().isBefore(yesterday)) reservationRepository.delete(reservation);
            else break;
        }
        log.info("미결제 예약 삭제 처리 정상 종료");
    }

}
