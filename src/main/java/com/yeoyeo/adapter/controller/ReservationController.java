package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.dto.MakeReservationAirbnbRequestDto;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeRequestDto;
import com.yeoyeo.application.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    @PostMapping("/payment")
    public GeneralResponseDto payment(@RequestBody PaymentRequestDto requestDto) {
        return paymentService.pay(requestDto);
    }
    @GetMapping("/payment")
    public Object test() {
        return paymentService.getPaymentData("imp_425844476859");
    }

    // Test 용도
    @PostMapping("/home")
    public GeneralResponseDto makeReservationHome(@RequestBody MakeReservationHomeRequestDto requestDto) {
        return reservationService.makeReservation(requestDto);
    }
    @PostMapping("/airbnb")
    public GeneralResponseDto makeReservationAirbnb(@RequestBody MakeReservationAirbnbRequestDto requestDto) {
        return reservationService.makeReservation(requestDto);
    }

}
