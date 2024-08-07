package com.yeoyeo.application.calendar.service;

import com.yeoyeo.aop.annotation.SingleJob;
import com.yeoyeo.application.collision.service.CollisionHandleService;
import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.common.exception.ExternalApiException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.Factory.GuestAirbnbFactory;
import com.yeoyeo.domain.Guest.Factory.GuestBookingFactory;
import com.yeoyeo.domain.Guest.Factory.GuestFactory;
import com.yeoyeo.domain.Guest.Guest;
import com.yeoyeo.domain.MapDateRoomReservation;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
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
import org.springframework.transaction.annotation.Propagation;
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

    private final ReservationService reservationService;
    private final CollisionHandleService collisionHandleService;
    private final MessageService messageService;
    private final SyncService syncService;

    private final DateRoomRepository dateRoomRepository;
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
        } else if (roomId == 2) {
            syncInICSFile_Airbnb_B();
            syncInICSFile_Booking_B();
        }
        else log.error("syncInICSFile_Reservation - Reservation Room ID is WRONG : given {}", roomId);
    }
    @Transactional
    @SingleJob(scheduleName = "regularSync_Airbnb")
    public synchronized void syncInICSFile_All() {
        syncInICSFile_Airbnb_A();
        syncInICSFile_Airbnb_B();
        syncInICSFile_Booking_B();
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncProcess(VEvent event, String uid, GuestFactory guestFactory, Payment payment, long roomId) {
        Reservation reservation = findExistingReservation(uid, roomId, guestFactory.getGuestClassName());
        if (reservation == null) registerReservation(event, guestFactory.createGuest(event.getDescription(), event.getSummary()), payment, roomId);
        else updateReservation(event, reservation);
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

    private void writeIcalendarFile(long roomId) {
        try {
            List<Reservation> reservationList = reservationRepository.findAllByReservationStateAndReservedFrom(1, "GuestHome");
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

    @Transactional
    public void setReservationStateSync(String guestClassName, long roomId) {
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1);
        LocalDate now = LocalDate.now();
        for (Reservation reservation : reservationList) {
            try {
                if (reservation.getRoom().getId()==roomId && reservation.getReservedFrom().equals(guestClassName) && reservation.getFirstDate().isAfter(now)) {
                    reservation.setStateSyncStart();
                }
            } catch (ReservationException e) {
                log.error("동기화를 위해 예약 상태를 변경 중 에러 발생 {}", reservation.getId(),e);
            }
        }
    }

    private void cancelReservationStateSync() {
        log.info("동기화 되지 않은 예약 취소작업");
        reservationRepository.flush();
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(5);
        log.info("취소 개수 : {}", reservationList.size());
        for (Reservation reservation : reservationList) {
            try {
                log.info("취소 예약 : {} / {} / {} ~ {}", reservation.getId(), reservation.getRoom().getName(), reservation.getFirstDate(), reservation.getLastDateRoom().getDate());
                reservationService.cancel(reservation);
            } catch (ReservationException e) {
                log.error("동기화 되지 않은 예약 취소 중 에러 발생 {}", reservation.getId(),e);
            }
        }
    }

    private Reservation findExistingReservation(String uid, long roomId, String guestClassName) {
        List<Reservation> reservationList = reservationRepository.findAllByUniqueId(uid);
        log.info("COUNT : {}", reservationList.size());
        for (Reservation reservation : reservationList) {
            log.info("INFO : {} {} {}", reservation.getReservationState(), reservation.getRoom().getId(), reservation.getGuest().getName());
            if (reservation.getReservationState() == 5
                && reservation.getRoom().getId() == roomId
                && reservation.getReservedFrom().equals(guestClassName)) return reservation;
        }
        log.info("일치하는 reservation 없음");
        return null;
    }

    private void registerReservation(VEvent event, Guest guest, Payment payment, long roomId) {
        log.info("Reservation Sync - Register : {} / roomId : {} / uid : {}", guest.getName(), roomId, event.getUid().getValue());
        for (int i=0;i<3;i++) {
            try {
                String startDate = event.getStartDate().getValue();
                String endDate = event.getEndDate().getValue();
                log.info("Reservation between : {} ~ {}", startDate, endDate);
                if (checkExceedingAvailableDate(startDate, endDate)) {
                    log.info("<예약 가능 기간 초과> - 예약 불가능한 날짜가 포함되어 있습니다.");
                    return;
                }
                List<DateRoom> dateRoomList = getDateRoomList(startDate, endDate, roomId);
                if (dateRoomList != null && dateRoomList.size() > 0) {
                    log.info("<등록 시작>");
                    try {
                        MakeReservationDto makeReservationDto = guest.createMakeReservationDto(
                            dateRoomList, event.getDescription(), event.getSummary());
                        Reservation reservation = reservationService.createReservation(
                            makeReservationDto);
                        reservation.setUniqueId(event.getUid().getValue());
                        reservationService.setReservationPaid(reservation, payment);
                        break;
                    } catch (ReservationException reservationException) {
                        log.info("[예약 충돌 발생] - 동기화 과정 중 중복된 예약 발생. 홈페이지 예약 변경/취소 작업 시작");
                        Guest collidedGuest = checkUidChangeIssue(dateRoomList,
                            guest.getName()); // UID 가 바뀌어서 기존 Guest 정보가 삭제되는 경우를 방지
                        log.info("UID 변경 검증 결과 : {}", collidedGuest != null);
                        if (collidedGuest != null)
                            guest = collidedGuest; // UID가 바뀌었어도 예약 출처와 정보가 일치한다면 동일한 게스트로 간주
                        boolean refundResult = collidedReservationCancel(dateRoomList);
                        if (refundResult) {
                            log.info("동기화 과정 중 중복된 예약 취소 완료. 재시도 시작");
                        } else {
                            log.info("동기화 과정 중 중복된 예약 취소 실패. 재시도 중지");
                            messageService.sendAdminMsg(
                                "동기화 중 중복된 예약 환불 미진행 (방변경 제안 / 시스템 장애) - 확인 필요. 체크인 날짜 : "
                                    + startDate);
                            break;
                        }
                    }
                } else
                    break;
                if (i == 2) {
                    log.info("동기화 중 중복된 예약 취소 실패. 재시도 횟수 초과");
                    messageService.sendAdminMsg("동기화 오류 알림 - 중복된 예약을 취소하던 중 오류 발생");
                }
            } catch (Exception e) {
                log.error("동기화 예약 등록 중 예상치 못한 에러 발생", e);
                messageService.sendDevMsg("동기화 중 예약 등록 중 예상치 못한 에러 발생 - 확인 필요");
                break;
            }
        }
    }

    private void updateReservation(VEvent event, Reservation reservation) {
        log.info("Reservation Sync - Update : {} {}~{} / {}", reservation.getRoom().getName(),reservation.getFirstDate(), reservation.getLastDateRoom().getDate(), reservation.getUniqueId());
        String eventStart = event.getStartDate().getValue();
        String eventEnd = event.getEndDate().getValue();
        try {
            if (!getLocalDateFromString(eventStart).isEqual(reservation.getFirstDate())
                || !getLocalDateFromString(eventEnd).isEqual(reservation.getLastDateRoom().getDate().plusDays(1))) {
                log.info("Update 중 날짜 변동사항 발견 - 예약취소 후 재등록 : {} ~ {} -> {} ~ {}", reservation.getFirstDate(), reservation.getLastDateRoom().getDate(), eventStart, eventEnd);
                reservationService.cancel(reservation);
                Guest guestClone = reservation.getGuest().clone();
                Payment paymentClone = reservation.getPayment().clone();
                registerReservation(event, guestClone, paymentClone, reservation.getRoom().getId());
            } else reservation.setStateSyncEnd(); // 동기화 완료
        } catch (ReservationException e) {
            messageService.sendAdminMsg("동기화 오류 알림 - 수정된 예약정보 반영을 위해 기존 예약 변경 중 오류 발생");
            log.error("달력 동기화 - 수정된 정보 반영 중 에러", e);
        }
    }

    private Guest checkUidChangeIssue(List<DateRoom> dateRoomList, String guestName) {
        log.info("UID 변경 검증");
        dateRoomList.sort(Comparator.comparing(DateRoom::getDate));
        Reservation collidedReservation = dateRoomList.get(0)
                .getMapDateRoomReservations().stream().map(MapDateRoomReservation::getReservation)
                .filter(reservation -> reservation.getReservationState() == 1 || reservation.getReservationState() == 5)
                .findFirst().orElse(null);
        if (collidedReservation != null) {
            log.info("UID 변경 검증 - 충돌 예약 정보 : {} / {} / {} / {} / {} / {} <- {}",
                collidedReservation.getUniqueId(), collidedReservation.getGuest().getName(),
                collidedReservation.getFirstDate(), collidedReservation.getLastDateRoom().getDate(),
                collidedReservation.getManagementLevel(), collidedReservation.getReservationState(), guestName);
        }
        if (collidedReservation != null
                && collidedReservation.getManagementLevel() > 0
                && collidedReservation.getGuest().getName().equals(guestName)
                && collidedReservation.getFirstDate().isEqual(dateRoomList.get(0).getDate())
                && collidedReservation.getLastDateRoom().getDate().isEqual(dateRoomList.get(dateRoomList.size()-1).getDate())) {
            return collidedReservation.getGuest();
        }
        return null;
    }

    private boolean collidedReservationCancel(List<DateRoom> dateRoomList) {
        for (DateRoom dateRoom : dateRoomList) {
            List<Reservation> reservationList = dateRoom.getMapDateRoomReservations().stream().map(MapDateRoomReservation::getReservation).collect(Collectors.toList());
            for (Reservation collidedReservation : reservationList) {
                if (collidedReservation.getReservationState() == 1 || collidedReservation.getReservationState() == 5) {
                    String uid = collidedReservation.getUniqueId();
                    log.info("{} 날짜의 {} 방 예약 취소 - 예약번호 : {} / {}", dateRoom.getDate(), dateRoom.getRoom().getName(), collidedReservation.getId(), uid);
                    if (uid == null || getPlatformName(uid).equals("yeoyeo")) {
                        log.info("홈페이지 예약 취소");
//                        GeneralResponseDto response = paymentService.refundBySystem(collidedReservation);
                        GeneralResponseDto response = collisionHandleService.collideRefund(collidedReservation);
                        if (!response.isSuccess()) return false;
                    } else {
                        log.info("플랫폼 예약 취소");
                        try {
                            reservationService.cancel(collidedReservation);
                        } catch (ReservationException e) {
                            log.error("플랫폼 예약 취소 작업 중 실패", e);
                            messageService.sendAdminMsg("동기화 오류 알림 - UID가 다른 플랫폼 예약 취소 작업 중 오류 발생");
                        }
                    }
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
        else {
            log.error("createIcsFile - Reservation Room ID is WRONG : given {}", roomId);
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        CalendarOutputter calendarOutputter = new CalendarOutputter();
        calendarOutputter.output(calendar, fileOutputStream);
    }

    private boolean checkExceedingAvailableDate(String start, String end) {
        LocalDate startDate = getLocalDateFromString(start);
        LocalDate lastDate = getLocalDateFromString(end);
        LocalDate today = LocalDate.now();
        LocalDate aYearAfter = today.plusMonths(6);
        return !startDate.isAfter(today) || !lastDate.isBefore(aYearAfter); // 시작일이 과거~오늘 or 종료일이 1년뒤~미래라면 True => 내일 ~ 1년 뒤의 하루 전 이면 False
    }

    private List<DateRoom> getDateRoomList(String start, String end, long roomId) {
        LocalDate startDate = getLocalDateFromString(start);
        LocalDate endDate = getLocalDateFromString(end).minusDays(1);
        LocalDate now = LocalDate.now();
        if (endDate.isBefore(now)) return new ArrayList<>();
        return dateRoomRepository.findAllByDateBetweenAndRoom_Id(startDate, endDate, roomId);
    }

    private GuestAirbnbFactory getGuestAirbnbFactory() {
        return new GuestAirbnbFactory();
    }

    private GuestBookingFactory getGuestBookingFactory() {
        return new GuestBookingFactory();
    }

    private Payment getPaymentAirbnb() {
        return Payment.builder()
                .amount(0).buyer_name("AirBnbGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("airbnb").receipt_url("none").status("paid").build();
    }

    private Payment getPaymentBooking() {
        return Payment.builder()
                .amount(0).buyer_name("BookingGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("booking").receipt_url("none").status("paid").build();
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

}
