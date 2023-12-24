package com.yeoyeo.domain;

import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.domain.Guest.Guest;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Cacheable // 2차 캐시 활성화
public class Reservation extends BaseTimeEntity {

    @Id
    private long id;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<MapDateRoomReservation> mapDateRoomReservations = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @Column
    private String reservedFrom;

    @Column(nullable = false)
    private long reservationState; // 0 : 미결제, 1 : 숙박 예정, 2 : 숙박 완료 , -1 : 예약 취소, -2 : 환불 완료, 5 : 동기화 중

    @Column
    private String uniqueId;

    @Column
    private int managementLevel; // 0 : 외부 플랫폼 관리, 1 : 홈페이지 관리 예약 (외부 동기화 미완료), 2: 홈페이지 관리 예약 (외부 동기화 완료)

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Builder
    public Reservation(List<DateRoom> dateRoomList, Guest guest, int managementLevel) {
        this.id = System.currentTimeMillis();
        this.mapDateRoomReservations.addAll(dateRoomList.stream().map(dateRoom -> new MapDateRoomReservation(dateRoom, this)).collect(Collectors.toList()));
        this.guest = guest;
        this.reservedFrom = guest.getClass().getSimpleName();
        this.reservationState = 0;
        this.managementLevel = managementLevel;
    }

    public void setUniqueId(String iCalendarUID) {
        this.uniqueId = iCalendarUID;
    }

    public List<DateRoom> getDateRoomList() {
        return this.mapDateRoomReservations.stream().map(MapDateRoomReservation::getDateRoom).collect(Collectors.toList());
    }

    public List<String> getDateRoomIdList() {
        return this.mapDateRoomReservations.stream().map(mapDateRoomReservation -> mapDateRoomReservation.getDateRoom().getId()).collect(Collectors.toList());
    }

    public void setPayment(Payment payment) throws ReservationException {
        this.payment = payment;
        setStatePaid();
    }

    public void setStatePaid() throws ReservationException {
        if (this.reservationState == 0) {
            this.reservationState = 1;
        } else {
            throw new ReservationException("미결제 상태가 아닙니다.");
        }
    }

    public void setStateComplete() throws ReservationException {
        if (this.reservationState == 1) {
            this.reservationState = 2;
        } else {
            throw new ReservationException("정상적인 예약이 아닙니다.");
        }
    }

    public void setStateCanceled() throws ReservationException {
        System.out.println(reservationState);
        if (this.reservationState == 1 || this.reservationState == 0 || this.reservationState == 5) {
            this.reservationState = -1;
        } else {
            throw new ReservationException("완료된 예약이 아닙니다.");
        }
    }

    public void setStateRefund() throws ReservationException {
        if (this.reservationState == 1 || this.reservationState == 0 || this.reservationState == 5) {
            this.reservationState = -2;
        } else {
            throw new ReservationException("환불 가능한 예약이 아닙니다.");
        }
    }

    public void setStateSyncStart() throws ReservationException {
        if (this.reservationState == 1) {
            this.reservationState = 5;
        } else {
            throw new ReservationException("동기화 시작 중 에러 발생");
        }
    }

    public void setStateSyncEnd() throws ReservationException {
        if (this.reservationState == 5) {
            this.reservationState = 1;
        } else {
            throw new ReservationException("동기화 종료 중 에러 발생");
        }
    }

    public void setManagementLevel(int managementLevel) {
        this.managementLevel = managementLevel;
    }

    public DateRoom getFirstDateRoom() {
        if (this.mapDateRoomReservations.size()==0) return null;
        return getDateRoomList().stream().sorted(Comparator.comparing(DateRoom::getDate)).collect(Collectors.toList()).get(0);
    }

    public DateRoom getLastDateRoom() {
        if (this.mapDateRoomReservations.size()==0) return null;
        return getDateRoomList().stream().sorted(Comparator.comparing(DateRoom::getDate)).collect(Collectors.toList()).get(this.mapDateRoomReservations.size()-1);
    }

    public LocalDate getFirstDate() {
        if (getFirstDateRoom()==null) return LocalDate.of(3999,12,31);
        return getFirstDateRoom().getDate();
    }

    public Room getRoom() {
        if (getFirstDateRoom()==null) return null;
        return getFirstDateRoom().getRoom();
    }

    public int getTotalPrice() {
        int totalPrice = 0;
        for (MapDateRoomReservation mapDateRoomReservation:this.mapDateRoomReservations) {
            totalPrice += mapDateRoomReservation.getDateRoom().getPrice();
            if (this.guest.getGuestCount()>2) totalPrice += 30000;
        }
        totalPrice -= 20000*(this.mapDateRoomReservations.size()-1);
        return totalPrice;
    }

    public boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]","").equals(this.guest.getNumberOnlyPhoneNumber());
    }

}
