package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.dto.PaymentWebHookDto;
import com.yeoyeo.application.payment.dto.RefundRequestDto;
import com.yeoyeo.application.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public GeneralResponseDto payment(@RequestBody PaymentRequestDto requestDto) {
        return paymentService.pay(requestDto);
    }

    @DeleteMapping("/refund")
    public GeneralResponseDto refund(@RequestBody RefundRequestDto requestDto) {
        return paymentService.refund(requestDto);
    }

    // Todo - 서버 호스팅 후 아임포트 웹훅 주소 수정 필요
    @PostMapping("/webhook")
    public void webhook(@RequestBody PaymentWebHookDto webHookDto) {

    }


}
