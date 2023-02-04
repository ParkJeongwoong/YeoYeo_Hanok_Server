package com.yeoyeo.application.reservation.eventLoop;

import com.yeoyeo.application.reservation.dto.ReservationSchedulerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotPayedReservation {

    private final Queue<ReservationSchedulerDto> notPayedReservationList = new LinkedList<>();

    public boolean add(ReservationSchedulerDto reservationSchedulerDto) {
        return this.notPayedReservationList.offer(reservationSchedulerDto);
    }

    public ReservationSchedulerDto getFirstReservation() {
        return this.notPayedReservationList.peek();
    }

    public ReservationSchedulerDto popFirstReservation() {
        return this.notPayedReservationList.poll();
    }

    public int countNotPayedReservation() {
        return this.notPayedReservationList.size();
    }

}
