package com.yeoyeo.domain.Guest.Factory;

import com.yeoyeo.domain.Guest.GuestAirbnb;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

public class GuestAirbnbFactory extends GuestFactory {

    public GuestAirbnbFactory() {
        super("AirBnbGuest", null, null, 2, null);
        super.guestClassName = "GuestAirbnb";
    }

    @Override
    public GuestAirbnb createGuest() {
        return new GuestAirbnb();
    }

    @Override
    public GuestAirbnb createGuest(Description description, Summary summary) {
        return new GuestAirbnb(description, summary);
    }

}
