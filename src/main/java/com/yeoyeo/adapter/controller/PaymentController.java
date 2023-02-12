package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.dto.PaymentWebHookDto;
import com.yeoyeo.application.payment.dto.RefundClientRequestDto;
import com.yeoyeo.application.payment.service.PaymentService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("payment")
public class PaymentController {

    private final PaymentService paymentService;

    @ApiOperation(value = "Payment", notes = "결제")
    @PostMapping("/pay")
    public ResponseEntity<GeneralResponseDto> payment(@RequestBody PaymentRequestDto requestDto) {
        GeneralResponseDto responseDto = paymentService.pay(requestDto);
        if (responseDto.getSuccessYN().equals("N")) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Refund", notes = "환불")
    @DeleteMapping("/refund")
    public ResponseEntity<GeneralResponseDto> refund(@RequestBody RefundClientRequestDto requestDto) {
        GeneralResponseDto responseDto = paymentService.refund(requestDto);
        if (responseDto.getSuccessYN().equals("N")) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    // Todo - 서버 호스팅 후 아임포트 웹훅 주소 수정 필요
    @ApiOperation(value = "WebHook", notes = "(서버전용) 아임포트 서버와 결제 정보 동기화 용도")
    @PostMapping("/webhook")
    public void webhook(@RequestBody PaymentWebHookDto webHookDto) {

    }


}
