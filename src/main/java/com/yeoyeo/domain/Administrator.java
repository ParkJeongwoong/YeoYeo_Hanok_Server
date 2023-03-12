package com.yeoyeo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@NoArgsConstructor
@Getter
@Entity
public class Administrator implements UserDetails {

    @Id
    private String id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 15)
    private String contact;

    @Builder
    public Administrator(String id, String name, String password, String contact) {
        this.id = id;
        this.name = name;
        this.password = new BCryptPasswordEncoder().encode(password);
        this.contact = contact;
    }

    public boolean checkPassword(String rawPassword) {
        log.info("raw : {}", rawPassword);
        log.info("encrypted : {}", new BCryptPasswordEncoder().encode(rawPassword));
        log.info("saved : {}", this.password);
        return new BCryptPasswordEncoder().matches(rawPassword, this.password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // Admin Page 뿐
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
