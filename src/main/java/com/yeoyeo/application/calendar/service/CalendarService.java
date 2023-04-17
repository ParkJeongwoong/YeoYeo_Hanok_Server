package com.yeoyeo.application.calendar.service;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationAirbnbDto;
import com.yeoyeo.application.reservation.dto.MakeReservationDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.Cacheable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class CalendarService {

    @Value("${ics.airbnb.path}")
    String AIRBNB_FILE_PATH;

    private final ReservationService reservationService;
    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;

    public void readAirbnbICSFile() {
        readIcalendarFile(AIRBNB_FILE_PATH, getGuestAirbnb(), getPaymentAirbnb());
    }

    private void readIcalendarFile(String path, Guest guest, Payment payment) {
        try {
            FileInputStream fileInputStream =new FileInputStream(path);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(fileInputStream);
            List<CalendarComponent> events = calendar.getComponents(Component.VEVENT);
            log.info("COUNT {}", events.size());
            for (CalendarComponent event : events) {
                Property status = event.getProperty("SUMMARY");
                if (status.getValue().equals("Reserved")) {
                    Property startDate = event.getProperty("DTSTART");
                    Property endDate = event.getProperty("DTEND");
                    log.info("{} ~ {}", startDate.getValue(), endDate.getValue());
                    List<DateRoom> dateRoomList = getDateRoomList(startDate.getValue(), endDate.getValue());
                    if (dateRoomList != null) {
                        MakeReservationDto makeReservationDto = new MakeReservationDto(dateRoomList, guest);
                        long reservationId = reservationService.createReservation(makeReservationDto);
                        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ReservationException("존재하지 않는 예약입니다."));
                        reservationService.setReservationPaid(reservation, payment);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            log.error("readIcalendarFile : Input File Not Found", e);
        } catch (ParserException|IOException e) {
            log.error("readIcalendarFile : CalendarBuilder Build Fail", e);
        } catch (ReservationException e) {
            log.error("readIcalendarFile : Create Reservation Exception", e);
        }
    }

    private List<DateRoom> getDateRoomList(String start, String end) {
        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyyMMdd")).minusDays(1);
        LocalDate now = LocalDate.now();
        if (endDate.isBefore(now)) return null;
        return dateRoomRepository.findAllByDateBetweenAndRoom_Id(startDate, endDate, 1);
    }

    private GuestAirbnb getGuestAirbnb() {
        return GuestAirbnb.builder().build();
    }

    private Payment getPaymentAirbnb() {
        return Payment.builder()
                .amount(0).buyer_name("AirBnbGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("airbnb").receipt_url("none").status("paid").build();
    }

    // Todo - Airbnb 에 자동 수신

    // Todo - Airbnb 송신 로직

    // Todo - Airbnb 에 자동 송신

}
