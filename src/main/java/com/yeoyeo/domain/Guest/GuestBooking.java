package com.yeoyeo.domain.Guest;

import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationBookingDto;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.domain.DateRoom;
import jakarta.persistence.Entity;
import java.util.List;
import lombok.Getter;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

@Getter
@Entity
public class GuestBooking extends Guest {

    public GuestBooking() {
        super.name = "BookingGuest";
    }

    public GuestBooking(Description description, Summary summary) {
        if (description == null || summary == null || !summary.getValue().equals("Reserved")) super.name = "BookingGuest_External";
        else super.name = "BookingGuest";
    }

    @Override
    public MakeReservationBookingDto createMakeReservationDto(List<DateRoom> dateRoomList) {
        return new MakeReservationBookingDto(dateRoomList, this, 0);
    }

    @Override
    public MakeReservationDto createMakeReservationDto(List<DateRoom> dateRoomList, Description description, Summary summary) {
        if (description == null || summary == null || !summary.getValue().equals("Reserved")) return new MakeReservationDto(dateRoomList, this, 1);
        return new MakeReservationDto(dateRoomList, this, 0);
    }

}
