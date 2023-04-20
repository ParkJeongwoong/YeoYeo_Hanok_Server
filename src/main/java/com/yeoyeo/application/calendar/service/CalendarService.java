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
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.FixedUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
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
    @Value("${ics.yeoyeo.path}")
    String YEOYEO_FILE_PATH;

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
    public void writeICSFile() { writeIcalendarFile(); }

    private void readIcalendarFile(String path, Guest guest, Payment payment, long roomId) {
        try {
            FileInputStream fileInputStream =new FileInputStream(path);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(fileInputStream);
            log.info(calendar.getProperties().toString());
            List<VEvent> events = calendar.getComponents(Component.VEVENT);
            log.info("COUNT {}", events.size());
            for (VEvent event : events) {
                String startDate = event.getStartDate().getValue();
                String endDate = event.getEndDate().getValue();
                log.info("Reservation : {} ~ {}", startDate, endDate);
                log.info(event.toString()); // TEST 용도
                if (checkExceedingAvailableDate(endDate)) break;
                List<DateRoom> dateRoomList = getDateRoomList(startDate, endDate, roomId);
                if (dateRoomList != null) {
                    MakeReservationDto makeReservationDto = new MakeReservationDto(dateRoomList, guest);
                    long reservationId = reservationService.createReservation(makeReservationDto);
                    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ReservationException("존재하지 않는 예약입니다."));
                    reservation.setUniqueId(event.getUid().getValue());
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

    private void writeIcalendarFile() {
        try {
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            TimeZone timeZone = registry.getTimeZone("Asia/Seoul");
            VTimeZone vTimeZone = timeZone.getVTimeZone();

            Calendar calendar = new Calendar()
                    .withProdId("-//Hanok stay Yeoyeo Reservation Events Calendar//iCal4j 3.2//KO")
                    .withDefaults()
                    .getFluentTarget();

            List<Reservation> reservationList = reservationRepository.findAllByReservationState(1);
            UidGenerator uidGenerator = new FixedUidGenerator("yeoyeo");

            for (Reservation reservation : reservationList) {
                String eventName = reservation.getGuest().getName();
                Date startDT = new Date(reservation.getFirstDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                Date endDT = new Date(reservation.getLastDateRoom().getDate().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                Uid uid;
                if (reservation.getUniqueId()==null||reservation.getUniqueId().length()==0) uid = uidGenerator.generateUid();
                else uid = new Uid(reservation.getUniqueId());

                VEvent event = new VEvent(startDT, endDT, eventName)
                        .withProperty(vTimeZone.getTimeZoneId())
                        .withProperty(uid)
                        .getFluentTarget();
                calendar.withComponent(event);
            }

            printICalendarData(calendar); // TEST 용도

            createIcsFile(calendar);

        } catch (SocketException e) {
            log.error("UidGenerator Issue : InetAddressHostInfo process Error", e);
        } catch (ParseException e) {
            log.error("Reservation LocalDate Parse Exception", e);
        } catch (IOException e) {
            log.error("ICS File Creation Exception", e);
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

    private void createIcsFile(Calendar calendar) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(YEOYEO_FILE_PATH);
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, fileOutputStream);
    }

    private boolean checkExceedingAvailableDate(String end) {
        LocalDate lastDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate aYearAfter = LocalDate.now().plusMonths(6);
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

    // TEST 용도
    private void printICalendarData(Calendar calendar) {
        log.info(calendar.getProperties().toString());
        List<CalendarComponent> events = calendar.getComponents(Component.VEVENT);
        log.info("COUNT {}", events.size());
        for (CalendarComponent event : events) {
            log.info(event.toString());
        }
    }

    // Todo - Airbnb 에 자동 수신

    // Todo - Airbnb 송신 로직

    // Todo - Airbnb 에 자동 송신

}
