package com.yeoyeo.application.guest.repository;

import com.yeoyeo.domain.Guest.GuestHome;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestHomeRepository extends JpaRepository<GuestHome, Long> {
}
