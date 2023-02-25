package com.yeoyeo.application.reservation.dto;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestHome;
import com.yeoyeo.domain.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MakeReservationHomeRequestDto extends MakeReservationRequestDto {

    public MakeReservationHomeRequestDto(String dateRoomId, String name, String phoneNumber, String email, int guestCount, String request) {
        super(dateRoomId, name, phoneNumber, email, guestCount, request);
    }

    public MakeReservationHomeDto getMakeReservationHomeDto(DateRoomRepository dateRoomRepository) {
        DateRoom dateRoom = dateRoomRepository.findByDateRoomId(this.dateRoomId);
        GuestHome guest = GuestHome.builder()
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .email(this.email)
                .guestCount(this.guestCount)
                .request(this.request)
                .build();
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
        return new MakeReservationHomeDto(dateRoom, guest, payment);
    }

}
