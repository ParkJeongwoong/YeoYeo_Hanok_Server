package com.yeoyeo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

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
