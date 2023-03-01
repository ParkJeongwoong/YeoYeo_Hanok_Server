package com.yeoyeo.application.payment;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.payment.repository.PaymentRepository;
import com.yeoyeo.application.payment.service.PaymentService;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeDto;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.GuestHome;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PaymentServiceTest {

    @Autowired
    DateRoomRepository dateRoomRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    ReservationService reservationService;
    @Autowired
    PaymentService paymentService;


    String dateRoom1Id = LocalDate.now().toString().replaceAll("[^0-9]","")+"1";
    String dateRoom2Id = LocalDate.now().toString().replaceAll("[^0-9]","")+"2";
    String dateRoom3Id = LocalDate.now().plusDays(1).toString().replaceAll("[^0-9]","")+"2";
    String dateRoom4Id = LocalDate.now().plusDays(2).toString().replaceAll("[^0-9]","")+"2";
    Payment payment1;
    Payment payment2;
    Payment payment3;
    Payment payment4;

    @BeforeEach
    public void setup() {
    }

    @AfterEach
    public void cleanup() {
        reservationRepository.deleteAll();
        paymentRepository.deleteAll();
        List<DateRoom> dateRooms = dateRoomRepository.findAll();
        dateRooms.forEach(dateRoom -> {
            try {
                if (dateRoom.getRoomReservationState()==1) {
                    dateRoom.resetState();
                }
            } catch (RoomReservationException e) {
                log.error("Dateroom 초기화 에러", e);
            }
        });
        dateRoomRepository.saveAll(dateRooms);
    }

    @Test
    @Transactional
    public void test_paymentDateRoomSetting_concurrency() throws InterruptedException {
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
        log.info("createReservation 동시성 테스트 진행");
        reservationService.createReservation(requestDto1);
        reservationService.createReservation(requestDto2);
        reservationService.createReservation(requestDto3);
        reservationService.createReservation(requestDto4);
        List<Reservation> reservations = reservationRepository.findAll();
        Reservation reservation1 = reservations.get(0);
        Reservation reservation2 = reservations.get(1);
        Reservation reservation3 = reservations.get(2);
        Reservation reservation4 = reservations.get(3);

        // When
        service.execute(() -> {
            try {
                reservationService.setReservationPaid(reservation1, payment1);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (jw) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                reservationService.setReservationPaid(reservation2, payment2);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (chw) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                reservationService.setReservationPaid(reservation3, payment3);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (dad) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                reservationService.setReservationPaid(reservation4, payment4);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (mom) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        latch.await();

        // Then
        log.info("createReservation 동시성 테스트 결과 검증");
        List<Payment> payments = paymentRepository.findAll();
        DateRoom dateRoom = dateRoomRepository.findById(dateRoom2Id).orElseThrow(NoSuchElementException::new);
        log.info("DateRoom 상태 : {} {}", dateRoom.getId(), dateRoom.getRoomReservationState());
        log.info("결제 성공한 숫자 : {}", payments.size());
        log.info("reservation1 status : {}", reservation1.getReservationState());
        log.info("reservation2 status : {}", reservation2.getReservationState());
        log.info("reservation3 status : {}", reservation3.getReservationState());
        log.info("reservation4 status : {}", reservation4.getReservationState());

        assertThat(payments.size()).isEqualTo(1);
    }
}
