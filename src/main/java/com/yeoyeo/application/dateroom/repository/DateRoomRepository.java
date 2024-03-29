package com.yeoyeo.application.dateroom.repository;

import com.yeoyeo.domain.DateRoom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DateRoomRepository extends JpaRepository<DateRoom, String> {
//    @Lock(LockModeType.OPTIMISTIC)
//    @Override
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    Optional<DateRoom> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"room"})
    Optional<DateRoom> findById(String id);
    List<DateRoom> findAllByOrderByDateAscRoom_Id();
    List<DateRoom> findAllByDateBetweenOrderByDateAscRoom_Id(LocalDate startDate, LocalDate endDate);
    List<DateRoom> findAllByDateBetweenAndRoom_Id(LocalDate startDate, LocalDate endDate, long roomId);
    List<DateRoom> findAllByDateBetween(LocalDate startDate, LocalDate endDate);
    List<DateRoom> findAllByDate(LocalDate date);
}
