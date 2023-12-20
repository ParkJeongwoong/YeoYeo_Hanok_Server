package com.yeoyeo.entity;

import com.yeoyeo.application.guest.repository.GuestRepository;
import com.yeoyeo.domain.Guest.Factory.GuestAirbnbFactory;
import com.yeoyeo.domain.Guest.Guest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CloneTest {

	@Autowired
	private GuestRepository guestRepository;

	@Test
	@Transactional
	public void test_guest_clone() {
		// Given
		GuestAirbnbFactory guestAirbnbFactory = new GuestAirbnbFactory();
		Guest guest = guestAirbnbFactory.createGuest();
		guestRepository.save(guest);

		// When
		Guest guestClone = guest.clone();
		guestRepository.save(guestClone);

		// Then
		List<Guest> guestList = guestRepository.findAll();
		log.info("guestList: {}", guestList);
	}

}
