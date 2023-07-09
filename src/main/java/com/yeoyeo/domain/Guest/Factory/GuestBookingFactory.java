package com.yeoyeo.domain.Guest.Factory;

import com.yeoyeo.domain.Guest.GuestBooking;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

public class GuestBookingFactory extends GuestFactory {

    public GuestBookingFactory() {
        super("BookingGuest", null, null, 2, null);
        super.guestClassName = "GuestBooking";
    }

    @Override
    public GuestBooking createGuest() {
        return new GuestBooking();
    }

    @Override
    public GuestBooking createGuest(Description description, Summary summary) {
        return new GuestBooking(description, summary);
    }

}
