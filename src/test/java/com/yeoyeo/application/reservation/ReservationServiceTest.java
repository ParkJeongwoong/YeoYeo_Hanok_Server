package com.yeoyeo.application.reservation;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.payment.repository.PaymentRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationAirbnbDto;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // BeforeAll 어노테이션을 non-static으로 사용하기 위한 어노테이션
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReservationServiceTest {

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    DateRoomRepository dateRoomRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    ReservationService reservationService;

    String dateRoom1Id = LocalDate.now().toString().replaceAll("[^0-9]","")+"1";
    String dateRoom2Id = LocalDate.now().toString().replaceAll("[^0-9]","")+"2";
    String dateRoom3Id = LocalDate.now().plusDays(1).toString().replaceAll("[^0-9]","")+"2";
    String dateRoom4Id = LocalDate.now().plusDays(2).toString().replaceAll("[^0-9]","")+"2";
    Payment payment;
    Payment payment_airbnb;

    @BeforeEach
    public void setup() {
//        Room room1 = roomRepository.findById(1L).orElseThrow(NoSuchElementException::new);
//        Room room2 = roomRepository.findById(2L).orElseThrow(NoSuchElementException::new);
//        dateRoomRepository.save(DateRoom.builder().date(LocalDate.now()).room(room1).webClientService(webClientService).key(holidayKey).build());
//        dateRoomRepository.save(DateRoom.builder().date(LocalDate.now()).room(room2).webClientService(webClientService).key(holidayKey).build());
//        dateRoomRepository.save(DateRoom.builder().date(LocalDate.now().plusDays(1)).room(room2).webClientService(webClientService).key(holidayKey).build());
//        dateRoomRepository.save(DateRoom.builder().date(LocalDate.now().plusDays(2)).room(room2).webClientService(webClientService).key(holidayKey).build());

        payment = Payment.builder()
                .amount(277777)
                .buyer_name("박정웅")
                .buyer_tel("010-1234-7777")
                .buyer_email("toto9091@daum.net")
                .buyer_addr("서울특별시 강남구 신사동")
                .imp_uid("imp_425844476859")
                .pay_method("card")
                .receipt_url("https://testadmin8.kcp.co.kr/assist/bill.BillActionNew.do?cmd=card_bill&tno=23665984854529&order_no=imp_425844476859&trade_mony=277777")
                .status("paid")
                .build();

        payment_airbnb = Payment.builder()
                .amount(250000)
                .buyer_name("박정웅")
                .buyer_tel("none")
                .buyer_email("none")
                .buyer_addr("none")
                .imp_uid("none")
                .pay_method("airbnb")
                .receipt_url("none")
                .status("paid")
                .build();
    }

    @AfterEach
    public void cleanup() {
        reservationRepository.deleteAll();
        paymentRepository.deleteAll();
        List<DateRoom> dateRooms = dateRoomRepository.findAll();
        dateRooms.forEach(dateRoom -> {
            if (dateRoom.getRoomReservationState()==1) {
                    dateRoom.resetState();
            }
        });
        dateRoomRepository.saveAll(dateRooms);
    }

    @AfterAll
    public void teardown() {
        reservationRepository.deleteAll();
        List<DateRoom> dateRooms = dateRoomRepository.findAll();
        dateRooms.forEach(dateRoom -> {
            if (dateRoom.getRoomReservationState()==1) {
                dateRoom.resetState();
            }
        });
        dateRoomRepository.saveAll(dateRooms);
    }

    @Test
    @Transactional
    public void test_createReservation() {
        log.info("createReservation 테스트 시작");
        // Given
        log.info("createReservation 테스트 준비");
        String guestName1 = "jeongwoong";
        String guestName2 = "AirBnbGuest";

        List<DateRoom> dateRoomList1 = new ArrayList<>();
        List<DateRoom> dateRoomList2 = new ArrayList<>();
        dateRoomList1.add(dateRoomRepository.findById(dateRoom1Id).orElseThrow(NoSuchElementException::new));
        dateRoomList2.add(dateRoomRepository.findById(dateRoom2Id).orElseThrow(NoSuchElementException::new));
        GuestHome guest1 = GuestHome.builder()
                .name(guestName1)
                .phoneNumber("010-1234-5678")
                .email("toto9091@naver.com")
                .guestCount(1)
                .request("없습니다.")
                .build();
        GuestAirbnb guest2 = GuestAirbnb.builder().build();

        MakeReservationHomeDto requestDto1 = new MakeReservationHomeDto(dateRoomList1, guest1);
        MakeReservationAirbnbDto requestDto2 = new MakeReservationAirbnbDto(dateRoomList2, guest2);

        // When
        log.info("createReservation 테스트 진행");
        long reservationId1 = 0;
        long reservationId2 =0;
        try {
            reservationId1 = reservationService.createReservation(requestDto1);
            reservationId2 = reservationService.createReservation(requestDto2);
        } catch (ReservationException e) {
            log.error(e.getMessage(), e);
        }

        // Then
        log.info("createReservation 테스트 결과 검증");
        Reservation reservation1 = reservationRepository.findById(reservationId1).orElseThrow(NoSuchElementException::new);
        Reservation reservation2 = reservationRepository.findById(reservationId2).orElseThrow(NoSuchElementException::new);
        assert reservation1 != null;
        assertThat(reservation1.getReservedFrom()).isEqualTo("GuestHome");
        assertThat(reservation1.getDateRoomList().get(0).getId()).isEqualTo(dateRoomList1.get(0).getId());
        assertThat(reservation1.getGuest().getName()).isEqualTo(guestName1);
        assert reservation2 != null;
        assertThat(reservation2.getReservedFrom()).isEqualTo("GuestAirbnb");
        assertThat(reservation2.getDateRoomList().get(0).getId()).isEqualTo(dateRoomList2.get(0).getId());
        assertThat(reservation2.getGuest().getName()).isEqualTo(guestName2);
    }

    @Test
    @Transactional
    public void test_createReservation_concurrency() {
        log.info("createReservation 동시성 테스트 시작");
        // Given
        log.info("createReservation 동시성 테스트 준비");
        int numberOfThreads = 4;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);


        List<DateRoom> dateRoomList1 = new ArrayList<>();
        dateRoomList1.add(dateRoomRepository.findById(dateRoom2Id).orElseThrow(NoSuchElementException::new));
        dateRoomList1.add(dateRoomRepository.findById(dateRoom3Id).orElseThrow(NoSuchElementException::new));
        dateRoomList1.add(dateRoomRepository.findById(dateRoom4Id).orElseThrow(NoSuchElementException::new));
        String guestName1 = "jw";
        String guestName2 = "chw";
        String guestName3 = "dad";
        String guestName4 = "mom";

        GuestHome guest1 = GuestHome.builder().name(guestName1).phoneNumber("010-1234-5678").email("toto9091@naver.com").guestCount(1).request("없습니다.").build();
        GuestHome guest2 = GuestHome.builder().name(guestName2).phoneNumber("010-1234-5678").email("toto9091@naver.com").guestCount(1).request("없습니다.").build();
        GuestHome guest3 = GuestHome.builder().name(guestName3).phoneNumber("010-1234-5678").email("toto9091@naver.com").guestCount(1).request("없습니다.").build();
        GuestHome guest4 = GuestHome.builder().name(guestName4).phoneNumber("010-1234-5678").email("toto9091@naver.com").guestCount(1).request("없습니다.").build();

        Payment payment1 = Payment.builder().amount(277777).buyer_name("박정웅").buyer_tel("010-1234-7777").buyer_email("toto9091@daum.net").buyer_addr("서울특별시 강남구 신사동").imp_uid("imp_425844476859").pay_method("card").receipt_url("https://testadmin8.kcp.co.kr/assist/bill.BillActionNew.do?cmd=card_bill&tno=23665984854529&order_no=imp_425844476859&trade_mony=277777").status("paid").build();
        Payment payment2 = Payment.builder().amount(277777).buyer_name("박정웅").buyer_tel("010-1234-7777").buyer_email("toto9091@daum.net").buyer_addr("서울특별시 강남구 신사동").imp_uid("imp_425844476859").pay_method("card").receipt_url("https://testadmin8.kcp.co.kr/assist/bill.BillActionNew.do?cmd=card_bill&tno=23665984854529&order_no=imp_425844476859&trade_mony=277777").status("paid").build();
        Payment payment3 = Payment.builder().amount(277777).buyer_name("박정웅").buyer_tel("010-1234-7777").buyer_email("toto9091@daum.net").buyer_addr("서울특별시 강남구 신사동").imp_uid("imp_425844476859").pay_method("card").receipt_url("https://testadmin8.kcp.co.kr/assist/bill.BillActionNew.do?cmd=card_bill&tno=23665984854529&order_no=imp_425844476859&trade_mony=277777").status("paid").build();
        Payment payment4 = Payment.builder().amount(277777).buyer_name("박정웅").buyer_tel("010-1234-7777").buyer_email("toto9091@daum.net").buyer_addr("서울특별시 강남구 신사동").imp_uid("imp_425844476859").pay_method("card").receipt_url("https://testadmin8.kcp.co.kr/assist/bill.BillActionNew.do?cmd=card_bill&tno=23665984854529&order_no=imp_425844476859&trade_mony=277777").status("paid").build();

        MakeReservationHomeDto requestDto1 = new MakeReservationHomeDto(dateRoomList1, guest1);
        MakeReservationHomeDto requestDto2 = new MakeReservationHomeDto(dateRoomList1, guest2);
        MakeReservationHomeDto requestDto3 = new MakeReservationHomeDto(dateRoomList1, guest3);
        MakeReservationHomeDto requestDto4 = new MakeReservationHomeDto(dateRoomList1, guest4);

        // When
        log.info("createReservation 동시성 테스트 진행");
        try {
            reservationService.createReservation(requestDto1);
            reservationService.createReservation(requestDto2);
            reservationService.createReservation(requestDto3);
            reservationService.createReservation(requestDto4);
        } catch (ReservationException e) {
            log.error(e.getMessage(), e);
        }

        // Then
        log.info("createReservation 동시성 테스트 결과 검증");
        List<Reservation> reservations = reservationRepository.findAll();
        DateRoom dateRoom = dateRoomRepository.findById(dateRoom2Id).orElseThrow(NoSuchElementException::new);
        log.info("DateRoom 상태 : {} {}", dateRoom.getId(), dateRoom.getRoomReservationState());
        log.info("예약 성공한 숫자 : {}", reservations.size());

        Reservation reservation1 = reservations.get(0);
        log.info("Reservation1 결과 : {} {} {}", reservation1.getGuest().getName(), reservation1.getFirstDateRoom().getId(), reservation1.getReservationState());
        if (reservations.size()>1) {
            Reservation reservation2 = reservations.get(1);
            log.info("Reservation2 결과 : {} {} {}", reservation2.getGuest().getName(), reservation2.getFirstDateRoom().getId(), reservation2.getReservationState());
        }
        if (reservations.size()>2) {
            Reservation reservation3 = reservations.get(2);
            log.info("Reservation3 결과 : {} {} {}", reservation3.getGuest().getName(), reservation3.getFirstDateRoom().getId(), reservation3.getReservationState());
        }
        if (reservations.size()>3) {
            Reservation reservation4 = reservations.get(3);
            log.info("Reservation4 결과 : {} {} {}", reservation4.getGuest().getName(), reservation4.getFirstDateRoom().getId(), reservation4.getReservationState());
        }

        assertThat(reservations.size()).isEqualTo(4);
    }

    @Test
    public void test_async() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(3);

        log.info("STEP 0 - Thread {}개 / {}", ((ThreadPoolExecutor) service).getPoolSize(), LocalDateTime.now().toLocalTime());
        service.submit(()->{
            new async_test_object().sleepTest(1);
            throw new IllegalArgumentException();
        });
        service.submit(()->{
            new async_test_object().sleepTest(2);
            throw new IllegalArgumentException();
        });
        service.submit(()->{
            new async_test_object().sleepTest(3);
            throw new IllegalArgumentException();
        });
        log.info("STEP 1 - Thread {}개 / {}", ((ThreadPoolExecutor) service).getPoolSize(), LocalDateTime.now().toLocalTime());
        // 1초 뒤 추가 작업
        Thread.sleep(1000);
        service.submit(()->{
            try {
                new async_test_object().sleepTest(4);
            } catch (InterruptedException e) {
                log.error("InterruptedException 발생", e);
            }
        });
        log.info("STEP 2 - Thread {}개 / {}", ((ThreadPoolExecutor) service).getPoolSize(), LocalDateTime.now().toLocalTime());
        // 1초 뒤 추가 작업
        Thread.sleep(1000);
        service.submit(()->{
            try {
                new async_test_object().sleepTest(5);
            } catch (InterruptedException e) {
                log.error("InterruptedException 발생", e);
            }
        });
        log.info("STEP 3 - Thread {}개 / {}", ((ThreadPoolExecutor) service).getPoolSize(), LocalDateTime.now().toLocalTime());
        Thread.sleep(10000);
        log.info("STEP 4 - Thread {}개 / {}", ((ThreadPoolExecutor) service).getPoolSize(), LocalDateTime.now().toLocalTime());

        assertThat(((ThreadPoolExecutor) service).getPoolSize()).isEqualTo(3);
    }

    private static class async_test_object {
        public void sleepTest(long id) throws InterruptedException {
            log.info("TEST START, ID : {} / {}", id, LocalDateTime.now().toLocalTime());
            Thread.sleep(5000);
            log.info("TEST END, ID : {} / {}", id, LocalDateTime.now().toLocalTime());
        }
    }
}