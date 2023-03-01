package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MakeReservationRequestDto {

    protected List<String> dateRoomIdList;
    protected String name;
    protected String phoneNumber;
    protected String email;
    protected int guestCount;
    protected String request;

    public MakeReservationRequestDto(List<String> dateRoomIdList, String name, String phoneNumber, String email, int guestCount, String request) {
        this.dateRoomIdList = dateRoomIdList;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.guestCount = guestCount;
        this.request = request;
    }

    public MakeReservationDto getMakeReservationDto(DateRoomRepository dateRoomRepository) {
        List<DateRoom> dateRoomList = dateRoomRepository.findAllById(this.dateRoomIdList);
        Guest guest = new Guest(this.name, this.phoneNumber, this.email, this.guestCount, this.request);
        return new MakeReservationDto(dateRoomList, guest);
    }

}
