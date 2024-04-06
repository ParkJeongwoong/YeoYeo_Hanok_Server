package com.yeoyeo.application.reservation.repository;

import com.yeoyeo.domain.Reservation;
import java.time.LocalDate;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @NonNull
    @EntityGraph(attributePaths = {"mapDateRoomReservations","mapDateRoomReservations.dateRoom", "mapDateRoomReservations.dateRoom.room", "guest", "payment"})
    List<Reservation> findAll();
    @EntityGraph(attributePaths = {"mapDateRoomReservations","mapDateRoomReservations.dateRoom", "mapDateRoomReservations.dateRoom.room", "guest", "payment"})
    List<Reservation> findAllByReservationState(long reservationState);
    @EntityGraph(attributePaths = {"mapDateRoomReservations","mapDateRoomReservations.dateRoom", "mapDateRoomReservations.dateRoom.room", "guest", "payment"})
    List<Reservation> findAllByReservationStateAndReservedFrom(long reservationState, String reservedFrom);
    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.mapDateRoomReservations mdr JOIN FETCH mdr.dateRoom dr JOIN FETCH dr.room JOIN FETCH r.guest JOIN FETCH r.payment WHERE r.reservationState = ?1 AND r.reservedFrom <> ?2")
    List<Reservation> findAllByReservationStateAndReservedFromNot(long reservationState, String reservedFrom);
    @EntityGraph(attributePaths = {"mapDateRoomReservations","mapDateRoomReservations.dateRoom", "mapDateRoomReservations.dateRoom.room", "guest", "payment"})
    List<Reservation> findAllByUniqueId(String uniqueId);
    @Query(value = "SELECT * FROM reservation WHERE first_date <= ?1 AND reserved_from = ?2", nativeQuery = true)
    List<Reservation> searchReservation(String searchWord, String phoneNumber);

    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.mapDateRoomReservations mdr JOIN FETCH mdr.dateRoom dr JOIN FETCH dr.room WHERE dr.room.id = ?1 AND dr.date BETWEEN ?2 AND ?3 AND r.reservationState = ?4")
    List<Reservation> findAllByRoomIdAndDateBetweenAndReservationState(long roomId, LocalDate startDate, LocalDate endDate, long reservationState);
    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.mapDateRoomReservations mdr JOIN FETCH mdr.dateRoom dr JOIN FETCH dr.room WHERE dr.room.id = ?1 AND r.reservationState = ?2")
    List<Reservation> findAllByRoomIdAndReservationState(long roomId, long reservationState);
    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.mapDateRoomReservations mdr JOIN FETCH mdr.dateRoom dr JOIN FETCH dr.room WHERE dr.room.id = ?1 AND r.reservationState = ?2 AND r.reservedFrom = ?3")
    List<Reservation> findAllByRoomIdAndReservationStateAndReservedFrom(long roomId, long reservationState, String reservedFrom);

}
