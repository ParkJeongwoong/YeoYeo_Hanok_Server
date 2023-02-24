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

    protected String dateRoomId;
    protected String name;
    protected String phoneNumber;
    protected String email;
    protected int guestCount;
    protected String request;

    public MakeReservationRequestDto(String dateRoomId, String name, String phoneNumber, String email, int guestCount, String request) {
        this.dateRoomId = dateRoomId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.guestCount = guestCount;
        this.request = request;
    }

    public MakeReservationDto getMakeReservationDto(DateRoomRepository dateRoomRepository) {
        DateRoom dateRoom = dateRoomRepository.findByDateRoomId(this.dateRoomId);
        Guest guest = new Guest(this.name, this.phoneNumber, this.email, this.guestCount, this.request);
        Payment payment = Payment.builder()
                .merchant_uid(dateRoom.getDateRoomId()+dateRoom.getReservationCount())
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
