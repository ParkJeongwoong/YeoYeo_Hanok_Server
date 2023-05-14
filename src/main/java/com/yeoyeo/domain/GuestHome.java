package com.yeoyeo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

//@SuperBuilder
@Getter
@NoArgsConstructor
@Entity
public class GuestHome extends Guest {

    @Builder
    public GuestHome(String name, String phoneNumber, String email, int guestCount, String request) {
        super(name, phoneNumber, email, guestCount, request);
    }

}
