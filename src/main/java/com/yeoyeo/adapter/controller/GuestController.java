package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.guest.dto.ImpCertRequestDto;
import com.yeoyeo.application.guest.service.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "고객 정보 API", description = "고객 정보 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("guest")
public class GuestController {

    private final GuestService guestService;

    @Operation(summary = "(미사용) 본인인증", description = "결제 본인 인증")
    @PostMapping("/certificate")
    public ResponseEntity<GeneralResponseDto> checkCertification(ImpCertRequestDto requestDto) {
        GeneralResponseDto responseDto = guestService.checkCertification(requestDto);
        if (!responseDto.isSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
