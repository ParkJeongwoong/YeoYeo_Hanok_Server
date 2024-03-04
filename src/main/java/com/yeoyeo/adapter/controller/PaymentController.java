package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.payment.dto.ImpConfirmDto;
import com.yeoyeo.application.payment.dto.ImpWebHookDto;
import com.yeoyeo.application.payment.dto.PaymentRequestDto;
import com.yeoyeo.application.payment.dto.RefundClientRequestDto;
import com.yeoyeo.application.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "결제 API", description = "결제 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제", description = "아임포트 결제 모듈로 결제 완료 후 결제정보+예약정보 전달")
    @PostMapping("/pay")
    public ResponseEntity<GeneralResponseDto> payment(@RequestBody PaymentRequestDto requestDto) {
        GeneralResponseDto responseDto = paymentService.pay_lock(requestDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(summary = "결제 전 확인", description = "아임포트 결제 모듈의 confirm_url 항목에 입력된 값. 결제 전 결제 가능 여부를 확인하는 역할")
    @PostMapping("/confirm")
    public ResponseEntity<GeneralResponseDto> confirm(@RequestBody ImpConfirmDto confirmDto) {
        GeneralResponseDto responseDto = paymentService.confirm(confirmDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }


    @Operation(summary = "환불", description = "환불 약관에 따른 환불")
    @DeleteMapping("/refund")
    public ResponseEntity<GeneralResponseDto> refund(@RequestBody RefundClientRequestDto requestDto) {
        GeneralResponseDto responseDto = paymentService.refund(requestDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(summary = "WebHook", description = "(서버전용) 아임포트 서버와 결제 정보 동기화 용도")
    @PostMapping("/webhook")
    public ResponseEntity<GeneralResponseDto> webhook(@RequestBody ImpWebHookDto webHookDto) {
        GeneralResponseDto responseDto = paymentService.webhook_lock(webHookDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
