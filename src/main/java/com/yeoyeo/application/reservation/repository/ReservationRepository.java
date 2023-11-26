package com.yeoyeo.application.reservation.repository;

import com.yeoyeo.domain.Reservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByReservationState(long reservationState);
    List<Reservation> findAllByReservationStateAndReservedFrom(long reservationState, String reservedFrom);
    List<Reservation> findAllByUniqueId(String uniqueId);
    @Query(value = "SELECT * FROM reservation WHERE first_date <= ?1 AND reserved_from = ?2", nativeQuery = true)
    List<Reservation> searchReservation(String searchWord, String phoneNumber);
}
