package com.yeoyeo.application.dateroom.dto;

import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Room;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class DateRoomCacheDto {

	private final String dateRoomId;
	private final LocalDate date;
	private final long roomId;
	private final String roomName;
	private final int price;
	private final int priceType;
	private final long reservationState;
	private final boolean reservable;

	public DateRoomCacheDto(DateRoom entity) {
		Room room = entity.getRoom();
		this.dateRoomId = entity.getId();
		this.date = entity.getDate();
		this.roomId = room.getId();
		this.roomName = room.getName();
		this.price = entity.getPrice();
		this.priceType = entity.getPriceType();
		this.reservationState = entity.getRoomReservationState();
		this.reservable = entity.isReservable();
	}

}
