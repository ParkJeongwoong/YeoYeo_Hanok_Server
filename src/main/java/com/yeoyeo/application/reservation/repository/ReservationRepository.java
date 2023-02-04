package com.yeoyeo.application.reservation.repository;

import com.yeoyeo.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
