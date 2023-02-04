package com.yeoyeo.application.guest.repository;

import com.yeoyeo.domain.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}
