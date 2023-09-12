package com.yeoyeo.application.dateroom.repository;

import com.yeoyeo.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {
}
