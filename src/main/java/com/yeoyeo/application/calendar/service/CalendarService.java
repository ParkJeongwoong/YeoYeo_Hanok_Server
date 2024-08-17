package com.yeoyeo.application.calendar.service;

import com.yeoyeo.aop.annotation.SingleJob;
import com.yeoyeo.application.common.exception.ExternalApiException;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.scraping.dto.ScrapingNaverBookingInfo;
import com.yeoyeo.domain.Guest.Factory.GuestAirbnbFactory;
import com.yeoyeo.domain.Guest.Factory.GuestBookingFactory;
import com.yeoyeo.domain.Guest.Factory.GuestFactory;
import com.yeoyeo.domain.Guest.Factory.GuestNaverFactory;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.FixedUidGenerator;
import net.fortuna.ical4j.util.SimpleHostInfo;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CalendarService {

    @Value("${ics.airbnb.a.url}")
    String AIRBNB_FILE_URL_A;
    @Value("${ics.airbnb.b.url}")
    String AIRBNB_FILE_URL_B;
    @Value("${ics.booking.b.url}")
    String BOOKING_FILE_URL_B;

    @Value("${ics.airbnb.a.path}")
    String AIRBNB_FILE_PATH_A;
    @Value("${ics.airbnb.b.path}")
    String AIRBNB_FILE_PATH_B;
    @Value("${ics.booking.b.path}")
    String BOOKING_FILE_PATH_B;
    @Value("${ics.yeoyeo.a.path}")
    String YEOYEO_FILE_PATH_A;
    @Value("${ics.yeoyeo.b.path}")
    String YEOYEO_FILE_PATH_B;
    @Value("${ics.naver.a.path}")
    String NAVER_FILE_PATH_A;
    @Value("${ics.naver.b.path}")
    String NAVER_FILE_PATH_B;

    private final MessageService messageService;
    private final SyncService syncService;

    private final ReservationRepository reservationRepository;

    public void readICSFile_Airbnb_A() { syncIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnbFactory(), getPaymentAirbnb(), 1);}
    public void readICSFile_Airbnb_B() { syncIcalendarFile(AIRBNB_FILE_PATH_B, getGuestAirbnbFactory(), getPaymentAirbnb(), 2);}
    public void getICSFile_Airbnb_A() { getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A); }
    public void getICSFile_Airbnb_B() { getIcsFileFromPlatform(AIRBNB_FILE_URL_B, AIRBNB_FILE_PATH_B); }

    public void readICSFile_Booking_B() { syncIcalendarFile(BOOKING_FILE_PATH_B, getGuestBookingFactory(), getPaymentBooking(), 2);}
    public void getICSFile_Booking_B() { getIcsFileFromPlatform(BOOKING_FILE_URL_B, BOOKING_FILE_PATH_B); }

    @Async("syncScheduleExecutor")
    @Transactional
    @SingleJob(scheduleName = "regularSync_Airbnb")
    public synchronized void syncInICSFile_Reservation(long roomId) {
        log.info("syncInICSFile_Reservation - Reservation Room ID : {}", roomId);
        if (roomId == 1) {
            syncInICSFile_Airbnb_A();
            syncInICSFile_Naver_A();
        } else if (roomId == 2) {
            syncInICSFile_Airbnb_B();
            syncInICSFile_Booking_B();
            syncInICSFile_Naver_B();
        }
        else log.error("syncInICSFile_Reservation - Reservation Room ID is WRONG : given {}", roomId);
    }
    @Transactional
    @SingleJob(scheduleName = "regularSync_Airbnb")
    public synchronized void syncInICSFile_All() {
        syncInICSFile_Airbnb_A();
        syncInICSFile_Airbnb_B();
        syncInICSFile_Booking_B();
        syncInICSFile_Naver_A();
        syncInICSFile_Naver_B();
    }

    @Transactional
    @SingleJob(scheduleName = "regularSync_Airbnb")
    public synchronized void syncInICSFile_Airbnb_A_sync() {
        syncInICSFile_Airbnb_A();
    }
    @Transactional
    @SingleJob(scheduleName = "regularSync_Airbnb")
    public synchronized void syncInICSFile_Airbnb_B_sync() {
        syncInICSFile_Airbnb_B();
    }
    @Transactional
    @SingleJob(scheduleName = "regularSync_Airbnb")
    public synchronized void syncInICSFile_Booking_B_sync() {
        syncInICSFile_Booking_B();
    }

    @Transactional
    public synchronized void syncInICSFile_Airbnb_A() {
        log.info("syncInICSFile_Airbnb_A - Start");
        getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A);
        syncIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnbFactory(), getPaymentAirbnb(), 1);
    }
    @Transactional
    public synchronized void syncInICSFile_Airbnb_B() {
        log.info("syncInICSFile_Airbnb_B - Start");
        getIcsFileFromPlatform(AIRBNB_FILE_URL_B, AIRBNB_FILE_PATH_B);
        syncIcalendarFile(AIRBNB_FILE_PATH_B, getGuestAirbnbFactory(), getPaymentAirbnb(), 2);
    }

    @Transactional
    public synchronized void syncInICSFile_Booking_B() {
        log.info("syncInICSFile_Booking_B - Start");
        getIcsFileFromPlatform(BOOKING_FILE_URL_B, BOOKING_FILE_PATH_B);
        syncIcalendarFile(BOOKING_FILE_PATH_B, getGuestBookingFactory(), getPaymentBooking(), 2);
    }

    @Transactional
    public synchronized void syncInICSFile_Naver_A() {
        log.info("syncInICSFile_Naver_A - Start");
        syncIcalendarFile(NAVER_FILE_PATH_A, getGuestNaverFactory(), getPaymentNaver(), 1);
    }
    @Transactional
    public synchronized void syncInICSFile_Naver_B() {
        log.info("syncInICSFile_Naver_B - Start");
        syncIcalendarFile(NAVER_FILE_PATH_B, getGuestNaverFactory(), getPaymentNaver(), 2);
    }

    @Transactional
    public void writeFullICSFile(long roomId) { writeFullIcalendarFile(roomId); }
    @Transactional
    public void writeICSFile(long roomId) { writeIcalendarFile(roomId); }
    public void sendICalendarData(HttpServletResponse response, long roomId) {
        try {
            String filePath;
            if (roomId==1) filePath = YEOYEO_FILE_PATH_A;
            else if (roomId==2) filePath = YEOYEO_FILE_PATH_B;
            else {
                log.error("sendICalendarData - Reservation Room ID is WRONG : given {}", roomId);
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

    /* [문제 상황] - 2024-04-23
    1. 에어비앤비에서 <수, 목, 금>이 예약 불가로 설정되어있고 <화>는 비어있음
    2. 화요일이 되면 당일 예약도 불가능해지므로 에어비앤비는 <화, 수, 목, 금> 이 예약 불가라고 생각
    3. 그리고 당일이 포함된 연속된 예약 상태 정보는 공유해주지 않음
    4. 화요일날 동기화를 하면 <수, 목, 금>에 예약 불가능하다는 정보가 오지 않음 (에어비앤비는 <화, 수, 목, 금> 예약 불가라고 생각 & 화요일이 포함됐기 때문에 전달 X) 
    5. 따라서 수목금 예약이 취소됐다고 생각하고 예약 정보를 삭제하는 문제 발생
    -> 에어비앤비 수동 예약인 경우 익일 예약 정보를 삭제하지 않음
     */
    private void syncIcalendarFile(String path, GuestFactory guestFactory, Payment dummyPayment, long roomId) {
        Calendar calendar = readIcalendarFile(path);
        if (calendar != null) {
            List<VEvent> events = calendar.getComponents(Component.VEVENT);
            log.info("EVENT COUNT : {}", events.size());
            syncService.setReservationStateSync(guestFactory.getGuestClassName(), roomId); // 동기화 대상 예약 상태 변경
            for (VEvent event : events) {
                String uid = event.getUid().getValue();
                log.info("CALENDAR EVENT UID : {}", uid);
                String platform = getPlatformName(uid);
                if (!platform.equals("yeoyeo")) {
                    syncService.syncProcess(event, uid, guestFactory, dummyPayment, roomId);
                }
            }
            syncService.cancelReservationStateSync(guestFactory.getGuestClassName(), roomId); // 동기화 되지 않은 예약 취소
        }
    }

    private Calendar readIcalendarFile(String path) {
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            CalendarBuilder builder = new CalendarBuilder();
            return builder.build(fileInputStream);
        } catch (FileNotFoundException e) {
            log.error("readIcalendarFile : Input File Not Found", e);
        } catch (ParserException|IOException e) {
            log.error("readIcalendarFile : CalendarBuilder Build Fail", e);
        }
        return null;
    }

    private void writeFullIcalendarFile(long roomId) {
        try {
            List<Reservation> reservationList = reservationRepository.findAllByReservationState(1);
            Calendar calendar = getCalendar(roomId);
            UidGenerator uidGenerator = new FixedUidGenerator(new SimpleHostInfo("yeoyeo"), "9091");
            for (Reservation reservation : reservationList) {
                VEvent event = createVEvent(reservation, uidGenerator);
                if (reservation.getRoom().getId()==roomId) calendar.withComponent(event);
            }
            createIcsFile(calendar, roomId);
        } catch (SocketException e) {
            log.error("UidGenerator Issue : InetAddressHostInfo process Error", e);
        } catch (IOException e) {
            log.error("ICS File Creation Exception", e);
        }
    }

    public void writeIcalendarFileByNaver(List<ScrapingNaverBookingInfo> scrapingData) {
        try {
            Calendar yeoyuCalendar = getNaverCalendar(1);
            Calendar yeohangCalendar = getNaverCalendar(2);
            UidGenerator uidGenerator = new FixedUidGenerator(new SimpleHostInfo("naver.com"), "9091");
            for (ScrapingNaverBookingInfo scrapingNaverBookingInfo : scrapingData) {
                VEvent event = createVEventByNaver(scrapingNaverBookingInfo, uidGenerator);
                if (scrapingNaverBookingInfo.getRoom().equals("한옥스테이 여여 - 여유")) yeoyuCalendar.withComponent(event);
                else yeohangCalendar.withComponent(event);
            }
            createIcsFile(yeoyuCalendar, 3);
            createIcsFile(yeohangCalendar, 4);
        } catch (SocketException e) {
            log.error("UidGenerator Issue : InetAddressHostInfo process Error", e);
        } catch (IOException e) {
            log.error("ICS File Creation Exception", e);
        }
    }

    private void writeIcalendarFile(long roomId) {
        try {
            List<Reservation> reservationList = reservationRepository.findAllByReservationStateAndReservedFrom(1, "GuestHome");
            List<Reservation> reservationFromNaverList = reservationRepository.findAllByReservationStateAndReservedFrom(1, "GuestNaver");
            Calendar calendar = getCalendar(roomId);
            UidGenerator uidGenerator = new FixedUidGenerator(new SimpleHostInfo("yeoyeo"), "9091");
            for (Reservation reservation : reservationList) {
                VEvent event = createVEvent(reservation, uidGenerator);
                if (reservation.getRoom().getId()==roomId) calendar.withComponent(event);
            }
            for (Reservation reservation : reservationFromNaverList) {
                VEvent event = createVEvent(reservation, uidGenerator);
                if (reservation.getRoom().getId()==roomId) calendar.withComponent(event);
            }
            createIcsFile(calendar, roomId);
        } catch (SocketException e) {
            log.error("UidGenerator Issue : InetAddressHostInfo process Error", e);
        } catch (IOException e) {
            log.error("ICS File Creation Exception", e);
        }
    }

    private byte[] generateByteICalendarFile(Calendar calendar) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CalendarOutputter calendarOutputter = new CalendarOutputter();
        calendarOutputter.output(calendar, outputStream);
        return outputStream.toByteArray();
    }

    @Retryable(retryFor = {ExternalApiException.class}, backoff = @Backoff(random = true, delay = 1000, maxDelay = 3000))
    private void getIcsFileFromPlatform(String downloadUrl, String downloadPath) {
        log.info("get ICS File From Platform : {}", downloadUrl);
        if (downloadUrl == null || downloadUrl.length() == 0) return;
        try {
            FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath));
            log.info("ICS File Download Complete From : {}", downloadUrl);
        } catch (IOException e) {
            log.error("ICS File Download Error", e);
            throw new ExternalApiException("ICS 파일 다운로드 중 오류 발생 - 파일 경로 : " + downloadPath, e);
        }
    }
    @Recover
    void recover(ExternalApiException e) {
        log.error("ICS 파일 다운로드 중 오류 발생 후 Recover");
        messageService.sendAdminMsg(e.getMessage());
    }

    private void createIcsFile(Calendar calendar, long roomId) throws IOException {
        String filePath;
        if (roomId==1) filePath = YEOYEO_FILE_PATH_A;
        else if (roomId==2) filePath = YEOYEO_FILE_PATH_B;
        else if (roomId==3) filePath = NAVER_FILE_PATH_A;
        else if (roomId==4) filePath = NAVER_FILE_PATH_B;
        else {
            log.error("createIcsFile - Reservation Room ID is WRONG : given {}", roomId);
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        CalendarOutputter calendarOutputter = new CalendarOutputter();
        calendarOutputter.output(calendar, fileOutputStream);
    }

    private GuestAirbnbFactory getGuestAirbnbFactory() {
        return new GuestAirbnbFactory();
    }

    private GuestBookingFactory getGuestBookingFactory() {
        return new GuestBookingFactory();
    }

    private GuestNaverFactory getGuestNaverFactory() {
        return new GuestNaverFactory();
    }

    private Payment getPaymentAirbnb() {
        return Payment.builder()
                .amount(0).buyer_name("AirBnbGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("airbnb").receipt_url("none").status("paid").build();
    }

    private Payment getPaymentBooking() {
        return Payment.builder()
                .amount(0).buyer_name("BookingGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("booking").receipt_url("none").status("paid").build();
    }

    private Payment getPaymentNaver() {
        return Payment.builder()
                .amount(0).buyer_name("NaverGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("naver").receipt_url("none").status("paid").build();
    }

    private Calendar getCalendar(long roomId) {
        return new Calendar()
                .withProdId("-//Hanok stay Yeoyeo Reservation Events Calendar - Roomd Id "+roomId+" //iCal4j 3.2//KO")
                .withDefaults()
                .getFluentTarget();
    }

    private Calendar getNaverCalendar(long roomId) {
        return new Calendar()
            .withProdId("-//Hanok stay Yeoyeo Reservation Events From Naver Calendar - Roomd Id "+roomId+" //iCal4j 3.2//KO")
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

    private VEvent createVEventByNaver(ScrapingNaverBookingInfo bookingInfo, UidGenerator uidGenerator) {
        try {
            String eventName = bookingInfo.getName();
            Date startDT = new Date(bookingInfo.getStartDate());
            Date endDT = new Date(bookingInfo.getEndDate());
            Uid uid = uidGenerator.generateUid();
            VEvent event = new VEvent(startDT, endDT, eventName)
                .withProperty(uid)
                .getFluentTarget();
            event.getProperties().add(new Description(bookingInfo.getPhone()+"/"+bookingInfo.getComment()+"/"+bookingInfo.getOption()));
            return event;
        } catch (ParseException e) {
            log.error("Reservation LocalDate Parse Exception", e);
        }
        return null;
    }

    private String getPlatformName(String uid) {
        String[] strings = uid.split("@");
        return strings[strings.length-1];
    }

}
