package com.yeoyeo.domain.Guest;

import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationNaverDto;
import com.yeoyeo.domain.DateRoom;
import jakarta.persistence.Entity;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class GuestNaver extends Guest {

	@Builder
	public GuestNaver(String name, String phoneNumber, String email, int guestCount, String request) {
		super(name, phoneNumber, email, guestCount, request);
	}

	@Override
	public MakeReservationNaverDto createMakeReservationDto(List<DateRoom> dateRoomList) {
		return new MakeReservationNaverDto(dateRoomList, this, 0);
	}

	@Override
	public GuestNaver clone() {
		return new GuestNaver(super.name, super.phoneNumber, super.email, super.guestCount, super.request);
	}

	@Override
	public String getPlatformName() {
		return "naver.com";
	}

}
