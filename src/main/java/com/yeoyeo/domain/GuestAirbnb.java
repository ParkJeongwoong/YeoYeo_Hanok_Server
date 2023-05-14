package com.yeoyeo.domain;

import com.yeoyeo.application.reservation.dto.MakeReservationAirbnbDto;
import com.yeoyeo.application.reservation.dto.MakeReservationDto;
import lombok.Builder;
import lombok.Getter;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

import javax.persistence.Entity;
import java.util.List;

//@SuperBuilder
@Getter
@Entity
public class GuestAirbnb extends Guest {

    @Builder
    public GuestAirbnb() {
        super.name = "AirBnbGuest";
    }

    @Override
    public MakeReservationAirbnbDto createMakeReservationDto(List<DateRoom> dateRoomList) {
        return new MakeReservationAirbnbDto(dateRoomList, this, 0);
    }

    @Override
    public MakeReservationDto createMakeReservationDto(List<DateRoom> dateRoomList, Description description, Summary summary) {
        if (description == null || summary == null || !summary.getValue().equals("Reserved")) return new MakeReservationDto(dateRoomList, this, 1);
        return new MakeReservationDto(dateRoomList, this, 0);
    }

}
