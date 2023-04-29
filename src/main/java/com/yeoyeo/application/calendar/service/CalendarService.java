package com.yeoyeo.application.calendar.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.payment.service.PaymentService;
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
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.FixedUidGenerator;
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
import java.util.stream.Collectors;

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
    private final PaymentService paymentService;
    private final MessageService messageService;

    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;

    public void readICSFile_Airbnb_A() { syncIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnb(), getPaymentAirbnb(), 1);}
    public void getICSFile_Airbnb_A() { getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A); }
    public void syncInICSFile_Airbnb_A() {
        getIcsFileFromPlatform(AIRBNB_FILE_URL_A, AIRBNB_FILE_PATH_A);
        syncIcalendarFile(AIRBNB_FILE_PATH_A, getGuestAirbnb(), getPaymentAirbnb(), 1);
    }
    public void syncInICSFile_Airbnb_B() {
        getIcsFileFromPlatform(AIRBNB_FILE_URL_B, AIRBNB_FILE_PATH_B);
        syncIcalendarFile(AIRBNB_FILE_PATH_B, getGuestAirbnb(), getPaymentAirbnb(), 2);
    }
    public void writeICSFile(long roomId) { writeIcalendarFile(roomId); }
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
        setReservationStateSync(guest.getName(), roomId); // 동기화 대상 예약 상태 변경
        for (VEvent event : events) {
            String uid = event.getUid().getValue();
            log.info("CALENDAR EVENT UID : {}", uid);
            String platform = getPlatformName(uid);
            if (!platform.equals("yeoyeo")) {
                Reservation reservation = findExistingReservation(uid, roomId, guest.getName());
                if (reservation == null) registerReservation(event, guest, payment, roomId);
                else updateReservation(event, reservation);
            }
        }
        cancelReservationStateSync(); // 동기화 되지 않은 예약 취소
    }

    private Calendar readIcalendarFile(String path) {
        try {
            FileInputStream fileInputStream =new FileInputStream(path);
            CalendarBuilder builder = new CalendarBuilder();
            return builder.build(fileInputStream);
        } catch (FileNotFoundException e) {
            log.error("readIcalendarFile : Input File Not Found", e);
        } catch (ParserException|IOException e) {
            log.error("readIcalendarFile : CalendarBuilder Build Fail", e);
        }
        return null;
    }

    private void writeIcalendarFile(long roomId) {
        try {
            List<Reservation> reservationList = reservationRepository.findAllByReservationState(1);
            Calendar calendar = getCalendar(roomId);
            UidGenerator uidGenerator = new FixedUidGenerator(new SimpleHostInfo("yeoyeo"), "9091");
            for (Reservation reservation : reservationList) {
                VEvent event = createVEvent(reservation, uidGenerator);
                if (reservation.getRoom().getId()==roomId) calendar.withComponent(event);
                else {
                    log.error("Reservation Room ID is WRONG");
                    break;
                }
            }
            createIcsFile(calendar, roomId);
        } catch (SocketException e) {
            log.error("UidGenerator Issue : InetAddressHostInfo process Error", e);
        } catch (IOException e) {
            log.error("ICS File Creation Exception", e);
        }
    }

    private void setReservationStateSync(String guestClassName, long roomId) {
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1);
        for (Reservation reservation : reservationList) {
            try {
                if (reservation.getRoom().getId()==roomId && reservation.getGuest().getName().equals(guestClassName)) {
                    reservation.setStateSyncStart();
                }
            } catch (ReservationException e) {
                log.error("동기화를 위해 예약 상태를 변경 중 에러 발생 {}", reservation.getId(),e);
            }
        }
    }

    private void cancelReservationStateSync() {
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(5);
        for (Reservation reservation : reservationList) {
            try {
                reservationService.cancel(reservation);
            } catch (ReservationException e) {
                log.error("동기화 되지 않은 예약 취소 중 에러 발생 {}", reservation.getId(),e);
            }
        }
    }

    private Reservation findExistingReservation(String uid, long roomId, String guestClassName) {
        List<Reservation> reservationList = reservationRepository.findByUniqueId(uid); // uid가 겹치는 경우가 발생 (uid는 한 calendar 내에서만 유일성 보장)
        for (Reservation reservation : reservationList) {
            if (reservation.getReservationState() == 1 && reservation.getRoom().getId() == roomId && reservation.getGuest().getName().equals(guestClassName)) return reservation;
        }
        return null;
    }

    private void registerReservation(VEvent event, Guest guest, Payment payment, long roomId) {
        log.info("Reservation Sync - Register : {} / {} / {}", guest.getName(), roomId, event.getUid().getValue());
        for (int i=0;i<3;i++) {
            String startDate = event.getStartDate().getValue();
            String endDate = event.getEndDate().getValue();
            log.info("Reservation between : {} ~ {}", startDate, endDate);
            if (checkExceedingAvailableDate(endDate)) return;
            List<DateRoom> dateRoomList = getDateRoomList(startDate, endDate, roomId);
            if (dateRoomList != null) {
                try {
                    MakeReservationDto makeReservationDto = new MakeReservationDto(dateRoomList, guest);
                    Reservation reservation = reservationService.createReservation(makeReservationDto);
                    reservation.setUniqueId(event.getUid().getValue());
                    reservationService.setReservationPaid(reservation, payment);
                    break;
                } catch (ReservationException reservationException) {
                    log.info("[예약 충돌 발생] - 동기화 과정 중 중복된 예약 발생. 홈페이지 예약 취소 처리 시작");
                    collidedReservationCancel(dateRoomList);
                }
            } else break;
            if (i == 2) messageService.sendAdminMsg("동기화 오류 알림 - 중복된 예약을 취소하던 중 오류 발생");
        }
    }

    private void updateReservation(VEvent event, Reservation reservation) {
        log.info("Reservation Sync - Update : {} / {}~{} / {}", reservation.getRoom().getName(),reservation.getFirstDate(), reservation.getLastDateRoom().getDate(), reservation.getUniqueId());
        String eventStart = event.getStartDate().getValue();
        String eventEnd = event.getEndDate().getValue();
        try {
            if (!getLocalDateFromString(eventStart).isEqual(reservation.getFirstDate())
            || !getLocalDateFromString(eventEnd).isEqual(reservation.getLastDateRoom().getDate().plusDays(1))) {
                log.info("Update 중 날짜 변동사항 발견 - 예약취소 후 재등록");
                reservationService.cancel(reservation);
                registerReservation(event, reservation.getGuest(), reservation.getPayment(), reservation.getRoom().getId());
            }
            reservation.setStateSyncEnd(); // 동기화 완료
        } catch (ReservationException e) {
            messageService.sendAdminMsg("동기화 오류 알림 - 수정된 예약정보 반영을 위해 기존 예약 변경 중 오류 발생");
            log.error("달력 동기화 - 수정된 정보 반영 중 에러", e);
        }
    }

    private boolean collidedReservationCancel(List<DateRoom> dateRoomList) {
        for (DateRoom dateRoom : dateRoomList) {
            List<Reservation> reservationList = dateRoom.getMapDateRoomReservations().stream().map(MapDateRoomReservation::getReservation).collect(Collectors.toList());
            for (Reservation collidedReservation : reservationList) {
                if (collidedReservation.getReservationState() == 1) {
                    log.info("{} 날짜의 {} 방 예약 취소 - 예약번호 : {}", dateRoom.getDate(), dateRoom.getRoom().getName(), collidedReservation.getId());
                    GeneralResponseDto response = paymentService.refundBySystem(collidedReservation);
                    if (!response.getSuccess()) return false;
                }
            }
        }
        return true;
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
        DataBufferUtils.write(dataBufferFlux, path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).block();

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
        LocalDate lastDate = getLocalDateFromString(end);
        LocalDate aYearAfter = LocalDate.now().plusMonths(6);
        return !lastDate.isBefore(aYearAfter);
    }

    private List<DateRoom> getDateRoomList(String start, String end, long roomId) {
        LocalDate startDate = getLocalDateFromString(start);
        LocalDate endDate = getLocalDateFromString(end).minusDays(1);
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

    private String getPlatformName(String uid) {
        String[] strings = uid.split("@");
        return strings[strings.length-1];
    }

    private LocalDate getLocalDateFromString(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    // TEST 용도
//    private void printICalendarData(Calendar calendar) {
//        log.info(calendar.getProperties().toString());
//        List<CalendarComponent> events = calendar.getComponents(Component.VEVENT);
//        log.info("COUNT {}", events.size());
//        for (CalendarComponent event : events) {
//            log.info(event.toString());
//        }
//    }

    // Todo - Airbnb 에 자동 수신

}
