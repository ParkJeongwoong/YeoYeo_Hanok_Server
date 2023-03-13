package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestHome;
import com.yeoyeo.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MakeReservationHomeRequestDto extends MakeReservationRequestDto {

    public MakeReservationHomeRequestDto(List<String> dateRoomIdList, String name, String phoneNumber, String email, int guestCount, String request) {
        super(dateRoomIdList, name, phoneNumber, email, guestCount, request);
    }

    public MakeReservationHomeDto getMakeReservationHomeDto(DateRoomRepository dateRoomRepository) throws ReservationException {
        if (this.dateRoomIdList.size()==0) throw new ReservationException("예약한 날짜 존재하지 않습니다.");
        List<DateRoom> dateRoomList = dateRoomRepository.findAllById(this.dateRoomIdList);
        GuestHome guest = GuestHome.builder()
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .guestCount(this.guestCount)
                .request(this.request)
                .build();
        return new MakeReservationHomeDto(dateRoomList, guest);
    }

}
