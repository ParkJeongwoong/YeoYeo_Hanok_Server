package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.dto.ImpConfirmDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.dto.ImpWebHookDto;
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

    @ApiOperation(value = "Payment", notes = "결제 - 아임포트 결제 모듈로 결제 완료 후 결제정보+예약정보 전달")
    @PostMapping("/pay")
    public ResponseEntity<GeneralResponseDto> payment(@RequestBody PaymentRequestDto requestDto) {
        GeneralResponseDto responseDto = paymentService.pay(requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "Confirm", notes = "결제 전 확인 - 아임포트 결제 모듈의 confirm_url 항목에 입력된 값. 결제 전 결제 가능 여부를 확인하는 역할")
    @PostMapping("/confirm")
    public ResponseEntity<GeneralResponseDto> confirm(@RequestBody ImpConfirmDto confirmDto) {
        GeneralResponseDto responseDto = paymentService.confirm(confirmDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }


    @ApiOperation(value = "Refund", notes = "환불")
    @DeleteMapping("/refund")
    public ResponseEntity<GeneralResponseDto> refund(@RequestBody RefundClientRequestDto requestDto) {
        GeneralResponseDto responseDto = paymentService.refund(requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @ApiOperation(value = "WebHook", notes = "(서버전용) 아임포트 서버와 결제 정보 동기화 용도")
    @PostMapping("/webhook")
    public ResponseEntity<GeneralResponseDto> webhook(@RequestBody ImpWebHookDto webHookDto) {
        GeneralResponseDto responseDto = paymentService.pay(webHookDto.getPaymentRequestDto());
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

//    @ApiOperation(value = "WebHook", notes = "(서버전용) 배포 전 기다리고 있는 예약 정보가 있는지 확인")
//    @GetMapping("/webhook")
//    public ResponseEntity<Integer> checkWebhook() { return ResponseEntity.status(HttpStatus.OK).body(webhookService.checkWebhook()); }

}
