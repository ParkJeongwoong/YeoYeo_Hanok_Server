package com.yeoyeo.application.admin.service;

import com.yeoyeo.application.admin.repository.AdministratorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdministratorRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return adminRepository.findById(username).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
    }

}
