package com.yeoyeo.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;

//@SuperBuilder
@Getter
@NoArgsConstructor
@Entity
public class GuestAirbnb extends Guest {

    @Builder
    public GuestAirbnb(String name) {
        super(name);
    }

}
