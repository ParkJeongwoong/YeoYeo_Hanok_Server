package com.yeoyeo.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
