package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.service.PaymentService;
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

    @DeleteMapping("/{reservationId}")
    public GeneralResponseDto cancelReservation(@PathVariable("reservationId") long reservationId) {
        return reservationService.cancel(reservationId);
    }

}
