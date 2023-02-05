package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("reservation")
public class ReservationController {

    private final PaymentService paymentService;

    @PostMapping("/payment")
    public GeneralResponseDto payment(@RequestBody PaymentRequestDto requestDto) {
        return paymentService.pay(requestDto);
    }

}
