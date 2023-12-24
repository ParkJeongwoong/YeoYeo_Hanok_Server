package com.yeoyeo.domain.Guest;

import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationAirbnbDto;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.domain.DateRoom;
import jakarta.persistence.Entity;
import java.util.List;
import lombok.Getter;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

//@SuperBuilder
@Getter
@Entity
public class GuestAirbnb extends Guest {

    public GuestAirbnb() {
        super.name = "AirBnbGuest";
    }

    public GuestAirbnb(Description description, Summary summary) {
        if (description == null || summary == null || !summary.getValue().equals("Reserved")) super.name = "AirBnbGuest_External";
        else super.name = "AirBnbGuest";
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
