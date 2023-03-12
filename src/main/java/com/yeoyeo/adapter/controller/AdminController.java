package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.admin.dto.SignupDto;
import com.yeoyeo.application.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("admin")
public class AdminController {

    private final AuthService authService;

    @PostMapping("/signup")
    public String test2(@RequestBody SignupDto requestDto) {
        return authService.signup(requestDto);
    }

}
