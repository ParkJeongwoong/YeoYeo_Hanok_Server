package com.yeoyeo.domain;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.general.webclient.WebClientService;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor
@Entity
public class DateRoom {
    @Id
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int priceType; // 0 : 직접 설정, 1 : 평일, 2 : 주말, 3 : 평일(특가), 4 : 주말(특가)

    @Column(nullable = false)
    private long roomReservationState; // 0 : 예약 가능, 1 : 예약 완료, 2 : 예약 대기 (Webhook)

    @Column(nullable = false)
    private boolean isReservable;

    @OneToMany(mappedBy = "dateRoom")
    private final List<MapDateRoomReservation> mapDateRoomReservations = new ArrayList<>();

    @Builder
    DateRoom(LocalDate date, Room room, WebClientService webClientService, String key) {
        this.id = date.toString().replaceAll("[^0-9]","") + room.getId();
        this.date = date;
        this.room = room;
        this.roomReservationState = 0;
        setDefaultPriceType(webClientService, key);
        setPrice();
        this.isReservable = true;
    }

    public void setStateBooked() throws RoomReservationException {
        if (this.roomReservationState != 1) {
            this.roomReservationState = 1;
        } else {
            throw new RoomReservationException("예약이 불가능한 날짜입니다.");
        }
    }

    public void setStateWaiting() throws RoomReservationException {
        if (this.roomReservationState == 0) {
            this.roomReservationState = 2;
        } else {
            throw new RoomReservationException("예약 대기가 불가능한 날짜입니다.");
        }
    }

    public void resetState() throws RoomReservationException {
        if (this.roomReservationState == 1) {
            this.roomReservationState = 0;
        } else if (this.roomReservationState == 2) { // Webhook 수신 후 예약 정보 수신 실패
            this.roomReservationState = 0;
        }
        else {
            throw new RoomReservationException("예약된 날짜가 아닙니다.");
        }
    }

    public long changePriceType(int priceType) {
        this.priceType = priceType;
        setPrice();
        return this.priceType;
    }

    public int changePrice(int price) {
        this.price = price;
        this.priceType = 0;
        return this.price;
    }

    private void setDefaultPriceType(WebClientService webClientService, String key) {
        DayOfWeek dayOfWeek = this.date.getDayOfWeek();
        switch (dayOfWeek) {
            case FRIDAY:
            case SATURDAY:
                this.priceType = 2;
                break;
            default:
                this.priceType = 1;
                break;
        }
        if (checkHoliday(webClientService, key)) {
            this.priceType = 1;
        }
    }

    private void setPrice() {
        switch (this.priceType) {
            case 1:
                this.price = this.room.getPrice();
                break;
            case 2:
                this.price = this.room.getPriceWeekend();
                break;
            case 3:
                this.price = this.room.getPriceWeekdaySpecial();
                break;
            case 4:
                this.price = this.room.getPriceWeekendSpecial();
                break;
        }
    }

    private boolean checkHoliday(WebClientService webClientService, String key) {
        LocalDate dayAfter = this.date.plusDays(1);
        String year = String.valueOf(dayAfter.getYear());
        String month = dayAfter.getMonthValue()>9 ? String.valueOf(dayAfter.getMonthValue()) : "0"+dayAfter.getMonthValue();
        String day = dayAfter.getDayOfMonth()>9 ? String.valueOf(dayAfter.getDayOfMonth()) : "0"+dayAfter.getDayOfMonth();
        String url = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?solYear="+year+"&solMonth="+month+"&_type=json&ServiceKey="+key;
        JSONParser parser = new JSONParser();

        JSONObject response = webClientService.get("application/json;charset=UTF-8", url);
        if (response == null) { throw new RuntimeException("Return 데이터 문제"); }
        try {
            response = (JSONObject) parser.parse(response.toString().replaceAll("\"","\\\""));
            JSONObject res = (JSONObject) response.get("response");
            JSONObject body = (JSONObject) res.get("body");
            String totalCount = body.get("totalCount").toString();
            if (totalCount.equals("0")) return false;
            JSONObject items = (JSONObject) body.get("items");
            if (totalCount.equals("1")) { // 1개면 그냥 객체로 응답됨
                JSONObject holiday = (JSONObject) items.get("item");
                String date = holiday.get("locdate").toString();
                return (date.equals(year + month + day));
            } else {
                JSONArray holidays = (JSONArray) items.get("item");
                for (Object holiday : holidays) { // 2개 이상이면 배열로 응답됨
                    String date = ((JSONObject) holiday).get("locdate").toString();
                    if ((date.equals(year + month + day))) return true;
                }
            }
        } catch (ParseException e) {
            log.error("공휴일 공공 데이터 확인 중 JSON Parsing Error 발생", e);
        }
        return false;
    }

    public void setReservable() {
        this.isReservable = true;
    }

    public void setUnReservable() {
        this.isReservable = false;
    }

}
