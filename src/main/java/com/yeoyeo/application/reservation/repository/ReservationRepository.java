package com.yeoyeo.application.reservation.repository;

import com.yeoyeo.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByDateRoom_DateRoomIdAndReservationState(String dateRoomId, long reservationState);
}
