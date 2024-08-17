package com.yeoyeo.application.scraping.dto;

import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.GuestNaver;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ScrapingNaverBookingInfo {

	private final String name;
	private final String phone;
	private final String reservationNumber;
	private final String startDate;
	private final String endDate;
	private final String room;
	private final String option;
	private final String comment;
	private final String price;
	private final String status;

	@Builder
	public ScrapingNaverBookingInfo(String name, String phone, String reservationNumber, String startDate, String endDate, String room, String option, String comment, String price, String status) {
		this.name = name;
		this.phone = phone;
		this.reservationNumber = reservationNumber;
		this.startDate = startDate;
		this.endDate = endDate;
		this.room = room;
		this.option = option;
		this.comment = comment;
		this.price = price;
		this.status = status;
	}

	public ScrapingNaverBookingInfo(LinkedHashMap jsonObject) {
		this.name = String.valueOf(jsonObject.get("name"));
		this.phone = String.valueOf(jsonObject.get("phone"));
		this.reservationNumber = String.valueOf(jsonObject.get("reservationNumber"));
		this.startDate = String.valueOf(jsonObject.get("startDate"));
		this.endDate = String.valueOf(jsonObject.get("endDate"));
		this.room = String.valueOf(jsonObject.get("room"));
		this.option = String.valueOf(jsonObject.get("option"));
		this.comment = String.valueOf(jsonObject.get("comment"));
		this.price = String.valueOf(jsonObject.get("price"));
		this.status = String.valueOf(jsonObject.get("status"));
	}

	public MakeReservationDto toMakeReservationDto(List<DateRoom> dateRoomList) {
		GuestNaver guest = toGuestNaver();
		return new MakeReservationDto(dateRoomList, guest, 1);
	}

	public GuestNaver toGuestNaver() {
		return new GuestNaver(name, phone, "", 0, comment + " / 예약번호 : " + reservationNumber + " / " + option);
	}

	public String makeUniqueKey() {
		return reservationNumber + "-" + startDate + "-" + endDate + "-9091@naver.com" ;
	}

}
