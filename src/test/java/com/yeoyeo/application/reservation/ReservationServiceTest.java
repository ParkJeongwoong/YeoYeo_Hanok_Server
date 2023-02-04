package com.yeoyeo.application.reservation;

import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationAirbnbRequestDto;
import com.yeoyeo.application.reservation.dto.MakeReservationHomeRequestDto;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.reservation.service.ReservationService;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Reservation;
import com.yeoyeo.domain.Room;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    RoomRepository roomRepository;
    @Autowired
    ReservationService reservationService;

    String dateRoom1Id;
    String dateRoom2Id;

    @BeforeAll
    public void firstSetup() {
        String roomName1 = "왼쪽방";
        String roomName2 = "오른쪽방";
        Room room1 = Room.builder().name(roomName1).build();
        Room room2 = Room.builder().name(roomName2).build();
        roomRepository.save(room1);
        roomRepository.save(room2);
        LocalDate now = LocalDate.now();
        DateRoom dateRoom1 = DateRoom.builder().date(now).room(room1).build();
        DateRoom dateRoom2 = DateRoom.builder().date(now).room(room2).build();
        dateRoom1Id = dateRoomRepository.save(dateRoom1).getDateRoomId();
        dateRoom2Id = dateRoomRepository.save(dateRoom2).getDateRoomId();
    }

    @AfterEach
    public void cleanup() {
        reservationRepository.deleteAll();
        List<DateRoom> dateRooms = dateRoomRepository.findAll();
        dateRooms.forEach(DateRoom::resetState);
        dateRoomRepository.saveAll(dateRooms);
    }

    @AfterAll
    public void teardown() {
        reservationRepository.deleteAll();
        dateRoomRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    public void test_makeReservation() {
        log.info("makeReservation 테스트 시작");
        // Given
        log.info("makeReservation 테스트 준비");
        String guestName1 = "jeongwoong";
        String guestName2 = "chaewoong";
        String phoneNumber = "010-1234-5678";
        MakeReservationHomeRequestDto requestDto1 = new MakeReservationHomeRequestDto(dateRoom1Id, guestName1, phoneNumber);
        MakeReservationAirbnbRequestDto requestDto2 = new MakeReservationAirbnbRequestDto(dateRoom2Id, guestName2);

        // When
        log.info("makeReservation 테스트 진행");
        long reservationId1 = reservationService.makeReservation(requestDto1).getResultId();
        long reservationId2 = reservationService.makeReservation(requestDto2).getResultId();

        // Then
        log.info("makeReservation 테스트 결과 검증");
        Reservation reservation1 = reservationRepository.findById(reservationId1).orElseThrow(NoSuchElementException::new);
        Reservation reservation2 = reservationRepository.findById(reservationId2).orElseThrow(NoSuchElementException::new);
        DateRoom dateRoom1 = dateRoomRepository.findById(dateRoom1Id).orElseThrow(NoSuchElementException::new);
        DateRoom dateRoom2 = dateRoomRepository.findById(dateRoom2Id).orElseThrow(NoSuchElementException::new);
        assert reservation1 != null;
        assertThat(reservation1.getReservedFrom()).isEqualTo("GuestHome");
        assertThat(reservation1.getDateRoom().getDateRoomId()).isEqualTo(dateRoom1.getDateRoomId());
        assertThat(reservation1.getGuest().getName()).isEqualTo(guestName1);
        assert reservation2 != null;
        assertThat(reservation2.getReservedFrom()).isEqualTo("GuestAirbnb");
        assertThat(reservation2.getDateRoom().getDateRoomId()).isEqualTo(dateRoom2.getDateRoomId());
        assertThat(reservation2.getGuest().getName()).isEqualTo(guestName2);
    }

    @Test
    public void test_makeReservation_concurrency() throws InterruptedException {
        log.info("makeReservation 동시성 테스트 시작");
        // Given
        log.info("makeReservation 동시성 테스트 준비");
        int numberOfThreads = 4;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        String guestName1 = "jw";
        String guestName2 = "chw";
        String guestName3 = "dad";
        String guestName4 = "mom";
        String phoneNumber = "010-1234-5678";
        MakeReservationHomeRequestDto requestDto1 = new MakeReservationHomeRequestDto(dateRoom1Id, guestName1, phoneNumber);
        MakeReservationHomeRequestDto requestDto2 = new MakeReservationHomeRequestDto(dateRoom1Id, guestName2, phoneNumber);
        MakeReservationHomeRequestDto requestDto3 = new MakeReservationHomeRequestDto(dateRoom1Id, guestName3, phoneNumber);
        MakeReservationHomeRequestDto requestDto4 = new MakeReservationHomeRequestDto(dateRoom1Id, guestName4, phoneNumber);

        // When
        log.info("makeReservation 동시성 테스트 진행");
        service.execute(() -> {
            try {
                reservationService.makeReservation(requestDto1);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (1)", e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                reservationService.makeReservation(requestDto2);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (2)", e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                reservationService.makeReservation(requestDto3);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (3)", e);
            }
            latch.countDown();
        });
        service.execute(() -> {
            try {
                reservationService.makeReservation(requestDto4);
            } catch (Exception e) {
                log.error("동시성 테스트 예외 (4)", e);
            }
            latch.countDown();
        });
        latch.await();

        // Then
        log.info("makeReservation 동시성 테스트 결과 검증");
        List<Reservation> reservations = reservationRepository.findAll();
        DateRoom dateRoom = dateRoomRepository.findById(dateRoom1Id).orElseThrow(NoSuchElementException::new);
        log.info("DateRoom 상태 : {} {}", dateRoom.getDateRoomId(), dateRoom.getRoomReservationState());
        log.info("예약 성공한 숫자 : {}", reservations.size());

        Reservation reservation1 = reservations.get(0);
        log.info("Reservation1 결과 : {} {} {}", reservation1.getGuest().getName(), reservation1.getDateRoom().getDateRoomId(), reservation1.getReservationState());
        if (reservations.size()>1) {
            Reservation reservation2 = reservations.get(1);
            log.info("Reservation2 결과 : {} {} {}", reservation2.getGuest().getName(), reservation2.getDateRoom().getDateRoomId(), reservation2.getReservationState());
            Assertions.assertThat(reservation1.getReservationState()).isNotEqualTo(reservation2.getReservationState());
        }
        if (reservations.size()>2) {
            Reservation reservation3 = reservations.get(2);
            log.info("Reservation3 결과 : {} {} {}", reservation3.getGuest().getName(), reservation3.getDateRoom().getDateRoomId(), reservation3.getReservationState());
            Assertions.assertThat(reservation1.getReservationState()).isNotEqualTo(reservation3.getReservationState());
        }
        if (reservations.size()>3) {
            Reservation reservation4 = reservations.get(3);
            log.info("Reservation4 결과 : {} {} {}", reservation4.getGuest().getName(), reservation4.getDateRoom().getDateRoomId(), reservation4.getReservationState());
            Assertions.assertThat(reservation1.getReservationState()).isNotEqualTo(reservation4.getReservationState());
        }
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