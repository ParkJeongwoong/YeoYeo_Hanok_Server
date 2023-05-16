package com.yeoyeo.application.payment;

import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.payment.repository.PaymentRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationHomeDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.GuestHome;
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


    String dateRoom1Id = LocalDate.now().toString().replaceAll("[^0-9]","")+"1";
    String dateRoom2Id = LocalDate.now().toString().replaceAll("[^0-9]","")+"2";
    String dateRoom3Id = LocalDate.now().plusDays(1).toString().replaceAll("[^0-9]","")+"2";
    String dateRoom4Id = LocalDate.now().plusDays(2).toString().replaceAll("[^0-9]","")+"2";

    @BeforeEach
    public void setup() {
    }

    @AfterEach
    public void cleanup() {
        try {
            reservationRepository.deleteAll();
            paymentRepository.deleteAll();
            List<DateRoom> dateRooms = dateRoomRepository.findAll();
            dateRooms.forEach(dateRoom -> {
                if (dateRoom.getRoomReservationState()==1) {
                    dateRoom.resetState();
                }
            });
            dateRoomRepository.saveAll(dateRooms);
        } catch (Exception e) {
            log.error("cleanup 에러", e);
        }
    }

    @Test
    @Transactional
    public void test_paymentDateRoomSetting_concurrency() throws InterruptedException {

        log.info("paymentDateRoomSetting 동시성 테스트 시작");
        // Given
        log.info("paymentDateRoomSetting 동시성 테스트 준비");
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
        log.info(">>>>>>>>>>>>>>>>>> paymentDateRoomSetting 동시성 테스트 진행");
        service.execute(() -> {
            try {
                Reservation reservation1 = reservationService.createReservation(requestDto1);
                log.info("ID 1 : {}", reservation1.getId());
                log.info("RESERVATION 1 : {}", reservation1.getDateRoomList().get(0).getRoomReservationState());
                    Thread.sleep(20);
                try {
                    reservationService.setReservationPaid(reservation1, payment1);
                    log.info(">>>>> 예약 성공 (jw) : {}", reservation1.getReservationState());
                } catch (Exception e) {
                    reservation1.setStateCanceled();
                    reservationRepository.save(reservation1);
                    log.error("메서드 종료 이후 예외 (jw) {}", e.getMessage(), e);
                    log.info(">>>>> 예약 실패 (jw) : {}", reservation1.getReservationState());
                }
            } catch (InterruptedException | ReservationException e) {
                log.error("reservation 객체 생성 중 예외 (jw) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                Thread.sleep(10);
                Reservation reservation2 = reservationService.createReservation(requestDto2);
                log.info("ID 2 : {}", reservation2.getId());
                log.info("RESERVATION 2 : {}", reservation2.getDateRoomList().get(0).getRoomReservationState());
                Thread.sleep(10);
                try {
                    reservationService.setReservationPaid(reservation2, payment2);
                    log.info(">>>>> 예약 성공 (chw) : {}", reservation2.getReservationState());
                } catch (Exception e) {
                    reservation2.setStateCanceled();
                    reservationRepository.save(reservation2);
                    log.error("메서드 종료 이후 예외 (chw) {}", e.getMessage(), e);
                    log.info(">>>>> 예약 실패 (chw) : {}", reservation2.getReservationState());
                }
            } catch (InterruptedException | ReservationException e) {
                log.error("reservation 객체 생성 중 예외 (chw) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                Thread.sleep(20);
                Reservation reservation3 = reservationService.createReservation(requestDto3);
                log.info("ID 3 : {}", reservation3.getId());
                log.info("RESERVATION 3 : {}", reservation3.getDateRoomList().get(0).getRoomReservationState());
                try {
                    reservationService.setReservationPaid(reservation3, payment3);
                    log.info(">>>>> 예약 성공 (dad) : {}", reservation3.getReservationState());
                } catch (Exception e) {
                    reservation3.setStateCanceled();
                    reservationRepository.save(reservation3);
                    log.info(">>>>> 예약 실패 (dad) : {}", reservation3.getReservationState());
                    log.error("메서드 종료 이후 예외 (dad) {}", e.getMessage(), e);
                }
            } catch (InterruptedException | ReservationException e) {
                log.error("reservation 객체 생성 중 예외 (dad) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                Thread.sleep(3000);
                Reservation reservation4 = reservationService.createReservation(requestDto4);
                log.info("ID 4 : {}", reservation4.getId());
                log.info("RESERVATION 4 : {}", reservation4.getDateRoomList().get(0).getRoomReservationState());
                Thread.sleep(1000);
                try {
                    reservationService.setReservationPaid(reservation4, payment4);
                    log.info(">>>>> 예약 성공 (mom) : {}", reservation4.getReservationState());
                } catch (Exception e) {
                    reservation4.setStateCanceled();
                    reservationRepository.save(reservation4);
                    log.info(">>>>> 예약 실패 (mom) : {}", reservation4.getReservationState());
                    log.error("메서드 종료 이후 예외 (mom) {}", e.getMessage(), e);
                }
            } catch (InterruptedException | ReservationException e) {
                log.error("reservation 객체 생성 중 예외 (mom) {}", e.getMessage(), e);
            }
            latch.countDown();
        });
        latch.await();

        // Then
        log.info(">>>>>>>>>>>>>>>>>> paymentDateRoomSetting 동시성 테스트 결과 검증");
        List<Reservation> reservations = reservationRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        DateRoom dateRoom = dateRoomRepository.findById(dateRoom2Id).orElseThrow(NoSuchElementException::new);
        log.info("DateRoom 상태 : {} {}", dateRoom.getId(), dateRoom.getRoomReservationState());
        log.info("예약 성공한 숫자 : {}", reservations.size());
        log.info("결제 성공한 숫자 : {}", payments.size());
        for (int i=1;i<reservations.size()+1;i++) {
            log.info("reservation{} status : {}", i, reservations.get(i-1).getReservationState());
        }

        try {
            dateRoom.setStateBooked();
        } catch (RoomReservationException e) {
            e.printStackTrace();
        }
        log.info("수정 된 DateRoom 상태 : {} {}", dateRoom.getId(), dateRoom.getRoomReservationState());
        
        /*
        reservation의 state가 하나만 1이고 나머지 3개는 -1인 걸 보면 낙관적 잠금이 정상적으로 동작한 걸로 보인다.
        payment가 여러 개가 저장되는 경우는 가끔 일어나지만 함수 외부에서 ObjectOptimisticLockingFailureException 을 잡는 건 유효한 걸로 보인다.
        setReservationPaid 외부에서 적절히 처리를 해주면 문제가 없을 걸로 보인다.
         */

        assertThat(payments.size()).isEqualTo(1);

    }

}
