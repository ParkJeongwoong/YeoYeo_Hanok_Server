package com.yeoyeo.application.guest.repository;

import com.yeoyeo.domain.GuestAirbnb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestAirbnbRepository extends JpaRepository<GuestAirbnb, Long> {
}
