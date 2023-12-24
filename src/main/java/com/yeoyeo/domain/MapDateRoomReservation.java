package com.yeoyeo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
public class MapDateRoomReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "dateRoom_id")
    private DateRoom dateRoom;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    public MapDateRoomReservation(DateRoom dateRoom, Reservation reservation) {
        this.dateRoom = dateRoom;
        this.reservation = reservation;
    }

}
