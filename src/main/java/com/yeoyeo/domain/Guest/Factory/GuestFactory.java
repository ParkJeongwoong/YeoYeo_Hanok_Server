package com.yeoyeo.domain.Guest.Factory;

import com.yeoyeo.domain.Guest.Guest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;
import org.springframework.stereotype.Component;

@Getter
@NoArgsConstructor
@Component
public class GuestFactory {

    String guestClassName;
    String name;
    String phoneNumber;
    String email;
    int guestCount;
    String request;

    public GuestFactory(String name, String phoneNumber, String email, int guestCount, String request) {
        this.guestClassName = Guest.class.getSimpleName();
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.guestCount = guestCount;
        this.request = request;
    }

    public Guest createGuest(String name, String phoneNumber, String email, int guestCount, String request) {
        return new Guest(name, phoneNumber, email, guestCount, request);
    }

    public Guest createGuest() {
        return new Guest(this.name, this.phoneNumber, this.email, this.guestCount, this.request);
    }

    public Guest createGuest(Description description, Summary summary) {
        return createGuest();
    }

    public void setDefaultData(String name, String phoneNumber, String email, int guestCount, String request) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.guestCount = guestCount;
        this.request = request;
    }

}
