package com.yeoyeo.domain.Guest;

import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationNaverDto;
import com.yeoyeo.domain.DateRoom;
import jakarta.persistence.Entity;
import java.util.List;
import lombok.Getter;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

@Getter
@Entity
public class GuestNaver extends Guest {

	public GuestNaver() {
		super.name = "NaverGuest";
	}

	public GuestNaver(String name, String phoneNumber, String email, int guestCount, String request) {
		super(name, phoneNumber, email, guestCount, request);
	}

	public GuestNaver(Description description, Summary summary) {
		if (summary == null) super.name = "NaverGuest_External";
		else super.name = summary.getValue();
		if (description == null) super.request = "네이버 예약입니다.";
		else {
			String[] descriptionLines = description.getValue().split("/");
			for (int i = 0; i < descriptionLines.length; i++) {
				if (i == 0) super.phoneNumber = descriptionLines[i];
				else if (i == 1) super.request = descriptionLines[i];
				else super.request += " / " + descriptionLines[i];
			}
		}
	}

	@Override
	public MakeReservationNaverDto createMakeReservationDto(List<DateRoom> dateRoomList) {
		return new MakeReservationNaverDto(dateRoomList, this, 1);
	}

	@Override
	public MakeReservationDto createMakeReservationDto(List<DateRoom> dateRoomList, Description description, Summary summary) {
		if (summary == null) super.name = "NaverGuest_External";
		else super.name = summary.getValue();
		if (description == null) super.request = "네이버 예약입니다.";
		else {
			String[] descriptionLines = description.getValue().split("/");
			for (int i = 0; i < descriptionLines.length; i++) {
				if (i == 0) super.phoneNumber = descriptionLines[i];
				else if (i == 1) super.request = descriptionLines[i];
				else super.request += " / " + descriptionLines[i];
			}
		}
		return new MakeReservationDto(dateRoomList, this, 1);
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
