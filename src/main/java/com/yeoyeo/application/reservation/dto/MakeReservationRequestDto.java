package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest;
import com.yeoyeo.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeReservationRequestDto {

    private String dateRoomId;
    protected String name;

    public MakeReservationRequestDto(String dateRoomId, String name) {
        this.dateRoomId = dateRoomId;
        this.name = name;
    }

    public MakeReservationDto getMakeReservationDto(DateRoomRepository dateRoomRepository) {
        DateRoom dateRoom = dateRoomRepository.findByDateRoomId(this.dateRoomId);
        Guest guest = new Guest(this.name);
        Payment payment = Payment.builder()
                .merchant_uid(dateRoom.getDateRoomId()+"&&"+dateRoom.getReservationCount())
                .amount(dateRoom.getPrice())
                .buyer_name(this.name)
                .buyer_tel("none")
                .buyer_email("none")
                .buyer_addr("none")
                .imp_uid("none")
                .pay_method("manual")
                .receipt_url("none")
                .status("paid")
                .build();
        return new MakeReservationDto(dateRoom, guest, payment);
    }

}
