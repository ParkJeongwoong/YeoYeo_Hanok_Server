package com.yeoyeo.domain;

import com.yeoyeo.application.reservation.dto.MakeReservationDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

import javax.persistence.*;
import java.util.List;

//@SuperBuilder
@Getter
//@MappedSuperclass
@NoArgsConstructor
@Entity
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(length = 30, nullable = false)
    protected String name;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 50)
    private String email;

    @Column
    private int guestCount;

    @Column(length = 255)
    private String request;

//    @Builder
    public Guest(String name, String phoneNumber, String email, int guestCount, String request) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.guestCount = guestCount;
        this.request = request;
    }

    public String getNumberOnlyPhoneNumber() {
        return this.phoneNumber.replaceAll("[^0-9]","");
    }

    public MakeReservationDto createMakeReservationDto(List<DateRoom> dateRoomList) {
        return new MakeReservationDto(dateRoomList, this, 1);
    }

    public MakeReservationDto createMakeReservationDto(List<DateRoom> dateRoomList, Description description, Summary summary) {
        return new MakeReservationDto(dateRoomList, this, 1);
    }
}