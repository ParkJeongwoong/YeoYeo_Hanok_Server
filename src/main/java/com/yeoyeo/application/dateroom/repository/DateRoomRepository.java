package com.yeoyeo.application.dateroom.repository;

import com.yeoyeo.domain.DateRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

public interface DateRoomRepository extends JpaRepository<DateRoom, String> {
//    @Lock(LockModeType.OPTIMISTIC)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    DateRoom findByDateRoomId(String dateRoomId);
    List<DateRoom> findAllOrderByDate();
    List<DateRoom> findAllByDateBetweenOrderByDate(LocalDate startDate, LocalDate endDate);
}
