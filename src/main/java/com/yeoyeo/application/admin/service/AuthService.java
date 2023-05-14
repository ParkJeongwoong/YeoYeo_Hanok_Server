package com.yeoyeo.application.admin.service;

import com.yeoyeo.application.admin.dto.SignupDto;
import com.yeoyeo.application.admin.repository.AdministratorRepository;
import com.yeoyeo.domain.Administrator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final AdministratorRepository adminRepository;

    public String signup(SignupDto signupDto) {
        Administrator admin = Administrator.builder().id(signupDto.getUserId()).password(signupDto.getUserPassword())
                .name(signupDto.getUserName()).contact(signupDto.getUserContact()).build();
        return adminRepository.save(admin).getId();
    }

}
