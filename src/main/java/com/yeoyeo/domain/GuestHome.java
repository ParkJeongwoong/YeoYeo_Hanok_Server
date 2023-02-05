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

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 30)
    private String email;

    @Column
    private long guestCount;

    @Column(length = 255)
    private String request;

    @Builder
    public GuestHome(String name, String phoneNumber, String email, long guestCount, String request) {
        super(name);
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.guestCount = guestCount;
        this.request = request;
    }

}
