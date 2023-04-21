package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.guest.dto.ImpCertRequestDto;
import com.yeoyeo.application.guest.service.GuestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"고객 정보 API"})
@RequiredArgsConstructor
@RestController
@RequestMapping("guest")
public class GuestController {

    private final GuestService guestService;

    @ApiOperation(value = "Phone Certification", notes = "(미사용) 휴대폰 본인인증")
    @PostMapping("/certificate")
    public ResponseEntity<GeneralResponseDto> checkCertification(ImpCertRequestDto requestDto) {
        GeneralResponseDto responseDto = guestService.checkCertification(requestDto);
        if (!responseDto.getSuccess()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
