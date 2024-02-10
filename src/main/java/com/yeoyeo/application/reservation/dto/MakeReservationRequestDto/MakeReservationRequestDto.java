package com.yeoyeo.application.reservation.dto.MakeReservationRequestDto;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.Guest;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public MakeReservationDto getMakeReservationDto(DateRoomRepository dateRoomRepository) throws ReservationException {
        if (this.dateRoomIdList.isEmpty()) throw new ReservationException("예약한 날짜 존재하지 않습니다.");
        List<DateRoom> dateRoomList = dateRoomRepository.findAllById(this.dateRoomIdList);
        Guest guest = new Guest(this.name, this.phoneNumber, this.email, this.guestCount, this.request);
        return new MakeReservationDto(dateRoomList, guest, 1);
    }

}
