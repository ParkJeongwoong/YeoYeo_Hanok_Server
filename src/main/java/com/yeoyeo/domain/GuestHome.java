package com.yeoyeo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;

//@SuperBuilder
@Getter
@NoArgsConstructor
@Entity
public class GuestHome extends Guest {

    @Column(length = 20)
    private String phoneNumber;

    @Builder
    public GuestHome(String name, String phoneNumber) {
        super(name);
        this.phoneNumber = phoneNumber;
    }

}
