package com.yeoyeo.domain.Guest;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
