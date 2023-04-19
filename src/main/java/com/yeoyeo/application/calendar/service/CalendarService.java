package com.yeoyeo.application.calendar.service;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CalendarService {

    @Value("${ics.airbnb.a.url}")
    String AIRBNB_FILE_URL_A;
    @Value("${ics.airbnb.b.url}")
    String AIRBNB_FILE_URL_B;
    @Value("${ics.airbnb.a.path}")
    String AIRBNB_FILE_PATH_A;
    @Value("${ics.airbnb.b.path}")
    String AIRBNB_FILE_PATH_B;

    private final ReservationService reservationService;
    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;

    public void readAirbnbICSFile() { readIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnb(), getPaymentAirbnb(), 1);}
    public void getAirbnbICSFile() { getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A); }
    public void syncInICSFile_Airbnb_A() {
        getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A);
        readIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnb(), getPaymentAirbnb(), 1);
    }
    public void syncInICSFile_Airbnb_B() {
        getIcsFileFromPlatform(AIRBNB_FILE_URL_B, AIRBNB_FILE_PATH_B);
        readIcalendarFile(AIRBNB_FILE_PATH_B, getGuestAirbnb(), getPaymentAirbnb(), 2);
    }

    private void readIcalendarFile(String path, Guest guest, Payment payment, long roomId) {
        try {
            FileInputStream fileInputStream =new FileInputStream(path);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(fileInputStream);
            List<CalendarComponent> events = calendar.getComponents(Component.VEVENT);
            log.info("COUNT {}", events.size());
            for (CalendarComponent event : events) {
                Property startDate = event.getProperty("DTSTART");
                Property endDate = event.getProperty("DTEND");
                log.info("Reservation : {} ~ {}", startDate.getValue(), endDate.getValue());
                log.info(event.toString());
                if (checkExceedingAvailableDate(endDate.getValue())) break;
                List<DateRoom> dateRoomList = getDateRoomList(startDate.getValue(), endDate.getValue(), roomId);
                if (dateRoomList != null) {
                    MakeReservationDto makeReservationDto = new MakeReservationDto(dateRoomList, guest);
                    long reservationId = reservationService.createReservation(makeReservationDto);
                    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ReservationException("존재하지 않는 예약입니다."));
                    reservationService.setReservationPaid(reservation, payment);
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

    private void getIcsFileFromPlatform(String downloadUrl, String downloadPath) {
        log.info("get ICS File From Platform : {}", downloadUrl);
        if (downloadUrl == null || downloadUrl.length() == 0) return;

        Flux<DataBuffer> dataBufferFlux = WebClient("application/json").get()
                .uri(downloadUrl)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        Path path = Paths.get(downloadPath);
        DataBufferUtils.write(dataBufferFlux, path, StandardOpenOption.CREATE).block();

        log.info("ICS File Download Complete From : {}", downloadUrl);
    }

    private boolean checkExceedingAvailableDate(String end) {
        LocalDate lastDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate aYearAfter = LocalDate.now().plusYears(1);
        return !lastDate.isBefore(aYearAfter);
    }

    private List<DateRoom> getDateRoomList(String start, String end, long roomId) {
        LocalDate startDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate endDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyyMMdd")).minusDays(1);
        LocalDate now = LocalDate.now();
        if (endDate.isBefore(now)) return null;
        return dateRoomRepository.findAllByDateBetweenAndRoom_Id(startDate, endDate, roomId);
    }

    private GuestAirbnb getGuestAirbnb() {
        return GuestAirbnb.builder().build();
    }

    private Payment getPaymentAirbnb() {
        return Payment.builder()
                .amount(0).buyer_name("AirBnbGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("airbnb").receipt_url("none").status("paid").build();
    }

    private WebClient WebClient(String contentType) {
        return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, contentType).build();
    }

    // Todo - Airbnb 에 자동 수신

    // Todo - Airbnb 송신 로직

    // Todo - Airbnb 에 자동 송신

}
