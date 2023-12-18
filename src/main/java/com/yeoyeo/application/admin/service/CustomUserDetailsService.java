package com.yeoyeo.application.admin.service;

import com.yeoyeo.application.admin.repository.AdministratorRepository;
import com.yeoyeo.domain.Admin.Administrator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdministratorRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findById(username).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));
    }

    public Administrator loadUserById(String id) {
        return adminRepository.findById(id).orElseThrow(()->new BadCredentialsException(id + " Invalid id"));
    }

}
