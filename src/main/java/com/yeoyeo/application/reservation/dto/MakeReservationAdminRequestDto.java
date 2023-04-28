package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestHome;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MakeReservationAdminRequestDto extends MakeReservationHomeRequestDto {

    public MakeReservationAdminRequestDto(List<String> dateRoomIdList, String request) {
        this.dateRoomIdList = dateRoomIdList;
        this.request = request;
    }

    @Override
    public MakeReservationHomeDto getMakeReservationDto(DateRoomRepository dateRoomRepository) throws ReservationException {
        if (this.dateRoomIdList.size()==0) throw new ReservationException("예약한 날짜 존재하지 않습니다.");
        List<DateRoom> dateRoomList = dateRoomRepository.findAllById(this.dateRoomIdList);
        GuestHome guest = GuestHome.builder()
                .name("관리자 생성 예약")
                .phoneNumber("000-0000-0000")
                .email("yeoyeo@gmail.com")
                .guestCount(1)
                .request("관리자가 생성한 예약입니다.")
                .build();
        return new MakeReservationHomeDto(dateRoomList, guest);
    }

}
