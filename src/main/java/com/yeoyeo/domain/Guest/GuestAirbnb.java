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

    private GuestAirbnb(String name, String last4Digits) {
        super.name = name;
        super.phoneNumber = last4Digits;
    }

    public GuestAirbnb(Description description, Summary summary) {
        if (description == null || summary == null || !summary.getValue().equals("Reserved")) super.name = "AirBnbGuest_External";
        else super.name = "AirBnbGuest";
        if (description != null) super.phoneNumber = getLast4DigitsFromAirbnb(description.getValue());
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

    @Override
    public GuestAirbnb clone() {
        return new GuestAirbnb(this.name, this.phoneNumber);
    }

    @Override
    public String getPlatformName() {
        return "airbnb.com";
    }

    private String getLast4DigitsFromAirbnb(String description) {
        if (description == null || description.length() < 4) {
            // 입력이 null이거나 길이가 4보다 작으면 전체 문자열 반환
            return description;
        }
        // 마지막 4자리를 반환
        return description.substring(description.length() - 4);
    }

}
