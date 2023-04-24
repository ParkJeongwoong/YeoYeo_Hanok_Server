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
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.FixedUidGenerator;
import net.fortuna.ical4j.util.HostInfo;
import net.fortuna.ical4j.util.SimpleHostInfo;
import net.fortuna.ical4j.util.UidGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
    @Value("${ics.yeoyeo.a.path}")
    String YEOYEO_FILE_PATH_A;
    @Value("${ics.yeoyeo.b.path}")
    String YEOYEO_FILE_PATH_B;

    private final ReservationService reservationService;
    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;

    public void syncAirbnbICSFile() { syncIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnb(), getPaymentAirbnb(), 1);}
    public void getAirbnbICSFile() { getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A); }
    public void syncInICSFile_Airbnb_A() {
        getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A);
        syncIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnb(), getPaymentAirbnb(), 1);
    }
    public void syncInICSFile_Airbnb_B() {
        getIcsFileFromPlatform(AIRBNB_FILE_URL_B, AIRBNB_FILE_PATH_B);
        syncIcalendarFile(AIRBNB_FILE_PATH_B, getGuestAirbnb(), getPaymentAirbnb(), 2);
    }
    public void writeICSFile() { writeIcalendarFile(); }
    public void sendICalendarData(HttpServletResponse response, long roomId) {
        try {
            String filePath;
            if (roomId==1) filePath = YEOYEO_FILE_PATH_A;
            else if (roomId==2) filePath = YEOYEO_FILE_PATH_B;
            else {
                log.error("Reservation Room ID is WRONG");
                return;
            }
            Calendar calendar = readIcalendarFile(filePath); // 외부 다운로드는 원본인 파일만 가능
            byte[] iCalFile = generateByteICalendarFile(calendar);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=calendar.ics");
            response.getOutputStream().write(iCalFile);
            response.flushBuffer();
        } catch (IOException e) {
            log.error("ICS File Byte Generation Exception", e);
        }
    }

    private void syncIcalendarFile(String path, Guest guest, Payment payment, long roomId) {
        Calendar calendar = readIcalendarFile(path);
        List<VEvent> events = calendar.getComponents(Component.VEVENT);
        log.info("COUNT {}", events.size());
        for (VEvent event : events) {
            registerReservation(event, guest, payment, roomId);
        }
    }

    private Calendar readIcalendarFile(String path) {
        try {
            FileInputStream fileInputStream =new FileInputStream(path);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(fileInputStream);
            log.info(calendar.getProperties().toString());
            return calendar;
        } catch (FileNotFoundException e) {
            log.error("readIcalendarFile : Input File Not Found", e);
        } catch (ParserException|IOException e) {
            log.error("readIcalendarFile : CalendarBuilder Build Fail", e);
        }
        return null;
    }

    private void writeIcalendarFile() {
        try {
            List<Reservation> reservationList = reservationRepository.findAllByReservationState(1);
            Calendar calendar1 = getCalendar(1);
            Calendar calendar2 = getCalendar(2);
            UidGenerator uidGenerator = new FixedUidGenerator(new SimpleHostInfo("yeoyeo"), "9091");
            for (Reservation reservation : reservationList) {
                VEvent event = createVEvent(reservation, uidGenerator);
                if (reservation.getFirstDateRoom().getRoom().getId()==1) calendar1.withComponent(event);
                else if (reservation.getFirstDateRoom().getRoom().getId()==2) calendar2.withComponent(event);
                else {
                    log.error("Reservation Room ID is WRONG");
                    break;
                }
            }
            printICalendarData(calendar1); // TEST 용도
            printICalendarData(calendar2); // TEST 용도
            createIcsFile(calendar1, 1);
            createIcsFile(calendar2, 2);
        } catch (SocketException e) {
            log.error("UidGenerator Issue : InetAddressHostInfo process Error", e);
        } catch (IOException e) {
            log.error("ICS File Creation Exception", e);
        }
    }

    private void registerReservation(VEvent event, Guest guest, Payment payment, long roomId) {
        try {
            String startDate = event.getStartDate().getValue();
            String endDate = event.getEndDate().getValue();
            log.info("Reservation : {} ~ {}", startDate, endDate);
            log.info(event.toString()); // TEST 용도
            if (checkExceedingAvailableDate(endDate)) return;
            List<DateRoom> dateRoomList = getDateRoomList(startDate, endDate, roomId);
            if (dateRoomList != null) {
                MakeReservationDto makeReservationDto = new MakeReservationDto(dateRoomList, guest);
                long reservationId = reservationService.createReservation(makeReservationDto);
                Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new ReservationException("존재하지 않는 예약입니다."));
                reservation.setUniqueId(event.getUid().getValue());
                reservationService.setReservationPaid(reservation, payment);
            }
        } catch (ReservationException e) {
            log.error("readIcalendarFile : Create Reservation Exception", e);
        }
    }

    private byte[] generateByteICalendarFile(Calendar calendar) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CalendarOutputter calendarOutputter = new CalendarOutputter();
        calendarOutputter.output(calendar, outputStream);
        return outputStream.toByteArray();
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

    private void createIcsFile(Calendar calendar, long roomId) throws IOException {
        String filePath;
        if (roomId==1) filePath = YEOYEO_FILE_PATH_A;
        else if (roomId==2) filePath = YEOYEO_FILE_PATH_B;
        else {
            log.error("Reservation Room ID is WRONG");
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        CalendarOutputter calendarOutputter = new CalendarOutputter();
        calendarOutputter.output(calendar, fileOutputStream);
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

    private Calendar getCalendar(long roomId) {
        return new Calendar()
                .withProdId("-//Hanok stay Yeoyeo Reservation Events Calendar - Roomd Id "+roomId+" //iCal4j 3.2//KO")
                .withDefaults()
                .getFluentTarget();
    }

    private VEvent createVEvent(Reservation reservation, UidGenerator uidGenerator) {
        try {
            String eventName = reservation.getGuest().getName();
            Date startDT = new Date(reservation.getFirstDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            Date endDT = new Date(reservation.getLastDateRoom().getDate().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            Uid uid;
            if (reservation.getUniqueId()==null||reservation.getUniqueId().length()==0) uid = uidGenerator.generateUid();
            else uid = new Uid(reservation.getUniqueId());
            return new VEvent(startDT, endDT, eventName)
                    .withProperty(uid)
                    .getFluentTarget();
        } catch (ParseException e) {
            log.error("Reservation LocalDate Parse Exception", e);
        }
        return null;
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
    // Todo - Airbnb 충돌 시 취소 로직

}
