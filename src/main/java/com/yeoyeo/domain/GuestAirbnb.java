package com.yeoyeo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

//@SuperBuilder
@Getter
@Entity
public class GuestAirbnb extends Guest {

    @Builder
    public GuestAirbnb() {
        String name = "AirBnbGuest";
        super.name = name;
    }

}
