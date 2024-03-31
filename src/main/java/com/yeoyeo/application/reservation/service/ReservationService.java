package com.yeoyeo.application.reservation.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.dto.DateRoomCacheDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.dateroom.service.DateRoomCacheService;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.reservation.dto.MakeReservationDto.MakeReservationDto;
import com.yeoyeo.application.reservation.dto.MakeReservationRequestDto.MakeReservationAdminRequestDto;
import com.yeoyeo.application.reservation.dto.MonthlyStatisticDto;
import com.yeoyeo.application.reservation.dto.MonthlyStatisticOriginDto;
import com.yeoyeo.application.reservation.dto.ReservationDetailInfoDto;
import com.yeoyeo.application.reservation.dto.ReservationInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.room.dto.RoomInfoDto;
import com.yeoyeo.application.room.service.RoomService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest.Guest;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final RoomService roomService;
    private final DateRoomCacheService dateRoomCacheService;
    private final MessageService messageService;

    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;

    public List<ReservationInfoDto> showReservations(int type) {
        // 현재 가상계좌 결제를 사용하지 않아 미결제 상태 0이 없음

        if (type == 0) { // 전체
            return reservationRepository.findAll().stream().sorted(Comparator.comparing(Reservation::getFirstDate)).map(ReservationInfoDto::new).collect(Collectors.toList());
        } else { // 1 : 숙박 대기, 2 : 숙박 완료, 3 : 예약 취소, 4 : 환불 완료
            return reservationRepository.findAllByReservationState(type).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).map(ReservationInfoDto::new).collect(Collectors.toList());
        }
    }

    public ReservationDetailInfoDto getReservationInfo(long reservationId, String phoneNumber) throws ReservationException {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new);
        if (reservation.validatePhoneNumber(phoneNumber)) return new ReservationDetailInfoDto(reservation);
        throw new ReservationException("전화번호가 일치하지 않습니다.");
    }

    @Transactional
    public Reservation createReservation(MakeReservationDto reservationDto) throws ReservationException {
        List<DateRoom> dateRoomList = reservationDto.getDateRoomList();
        if (dateRoomList.isEmpty()) throw new ReservationException("존재하지 않는 방입니다.");
        for (DateRoom dateRoom:dateRoomList) {
            if (!dateRoom.isReservable()) throw new ReservationException("예약이 불가능한 날짜입니다.");
            if (dateRoom.getRoomReservationState() != 0 ) throw new ReservationException("이미 예약이 완료된 방입니다.");
        }
        Guest guest = reservationDto.getGuest();
        Reservation reservation = Reservation.builder()
                .dateRoomList(dateRoomList)
                .guest(guest)
                .managementLevel(reservationDto.getManagement_level())
                .build();
        reservation = reservationRepository.save(reservation);
        log.info("{} 고객님의 예약 정보가 생성되었습니다.", reservationDto.getGuest().getName());
        return reservation;
    }

    @Transactional
    public Reservation createReservation(MakeReservationAdminRequestDto reservationDto) throws ReservationException {
        Reservation reservation = createReservation(reservationDto.getMakeReservationDto(dateRoomRepository));
        Payment adminPayment = Payment.builder()
                .amount(reservation.getTotalPrice()).buyer_name("AdminGuest").buyer_tel("000-0000-0000").imp_uid("none").pay_method("airbnb").receipt_url("none").status("paid").build();
        setReservationPaid(reservation, adminPayment);
        return reservation;
    }

    @Transactional
    public void setReservationPaid(Reservation reservation, Payment payment) throws ReservationException {
//        List<String> dateRoomIdList = reservation.getDateRoomIdList();
        List<DateRoom> dateRoomList = reservation.getDateRoomList();
        try {
//            for (String dateRoomId:dateRoomIdList) {
//                DateRoom dateRoom = dateRoomRepository.findById(dateRoomId).orElseThrow(NoSuchElementException::new);
            for (DateRoom dateRoom:dateRoomList) {
                log.info("{} : {} {} 예약시도", reservation.getGuest().getName(), dateRoom.getDate(), dateRoom.getRoom().getId());
                dateRoom.setStateBooked();
//                dateRoomRepository.save(dateRoom);
                log.info("{} : {} {} 예약성공", reservation.getGuest().getName(), dateRoom.getDate(), dateRoom.getRoom().getId());
                dateRoomCacheService.updateCache(new DateRoomCacheDto(dateRoom));
            }
            reservation.setPayment(payment);
            dateRoomRepository.saveAll(dateRoomList);
            reservationRepository.save(reservation);
            log.info("{} 고객 예약성공!!", reservation.getGuest().getName());
        } catch (RoomReservationException e) {
            log.error("이미 예약된 날짜입니다.", e);
            throw new ReservationException(e.getMessage());
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException e) {
            log.error("예약된 날짜 에러(낙관적 락) - {}", reservation.getGuest().getName(), e);
            throw new ReservationException(e.getMessage());
        } catch (ReservationException e) {
            log.error("데이터 변경 중 에러", e);
        }
    }

    @Transactional
    public GeneralResponseDto cancel(long reservationId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(NoSuchElementException::new);
            checkPhoneNumber(reservation);
            List<DateRoom> dateRoomList = reservation.getDateRoomList();
            reservation.setStateCanceled();
            for (DateRoom dateRoom:dateRoomList) {
                dateRoom.resetState();
                dateRoomCacheService.updateCache(new DateRoomCacheDto(dateRoom));
            }
            reservationRepository.save(reservation);
            messageService.sendCancelMsg(reservation);
            log.info("{} 고객님의 예약이 취소되었습니다.", reservation.getGuest().getName());
            return GeneralResponseDto.builder()
                    .success(true)
                    .message("예약이 취소되었습니다.")
                    .build();
        } catch (ReservationException reservationException) {
            log.error("Reservation 상태 변경 에러", reservationException);
            return GeneralResponseDto.builder()
                    .success(false)
                    .message(reservationException.getMessage())
                    .build();
        }
    }

    @Transactional
    public void cancel(Reservation reservation) throws ReservationException {
        try {
            reservation.setStateRefund();
            for (DateRoom dateRoom:reservation.getDateRoomList()) {
                dateRoom.resetState();
                dateRoomCacheService.updateCache(new DateRoomCacheDto(dateRoom));
            }
            reservationRepository.save(reservation);
            log.info("{} 고객님의 예약이 취소되었습니다.", reservation.getGuest().getName());
        } catch (ReservationException reservationException) {
            log.error("Reservation 상태 변경 에러", reservationException);
            throw new ReservationException(reservationException.getMessage());
        }
    }

    public Reservation getReservation(long reservationId) {
        return reservationRepository.findById(reservationId).orElse(null);
    }

    public void changeStateWait(Reservation reservation) throws ReservationException {
        try {
            for (DateRoom dateRoom:reservation.getDateRoomList()) {
                dateRoom.resetState();
                dateRoom.getMapDateRoomReservations().clear(); // Entity의 영속성 관계를 끊어줌 (실제 DB 작업은 Reservation 에서 이루어짐)
                dateRoomCacheService.updateCache(new DateRoomCacheDto(dateRoom));
            }
            dateRoomRepository.saveAll(reservation.getDateRoomList());

            List<DateRoom> otherDateRoomList = getAnotherRoomDateRoomList(reservation);
            reservation.changeDateRoomList(otherDateRoomList);
            reservation.setStateChangeWait();
            for (DateRoom dateRoom:reservation.getDateRoomList()) {
                dateRoom.setStateWaiting();
                dateRoomCacheService.updateCache(new DateRoomCacheDto(dateRoom));
            }
            reservationRepository.save(reservation);
        } catch (RoomReservationException e) {
            log.error("예약 변경 대기 중 에러 발생", e);
            throw new ReservationException("예약 변경 대기 중 에러 발생");
        }
    }

    public List<DateRoom> getAnotherRoomDateRoomList(Reservation reservation) {
        long roomId = reservation.getDateRoomList().get(0).getRoom().getId();
        long anotherRoomId = roomId == 1 ? 2 : 1;
        return dateRoomRepository.findAllByDateBetweenAndRoom_Id(reservation.getFirstDate(), reservation.getLastDateRoom().getDate(), anotherRoomId);
    }

    // PhoneNumber가 없는 에어비앤비 예약 필터링
    private void checkPhoneNumber(Reservation reservation) throws ReservationException {
        String phoneNumber = reservation.getGuest().getPhoneNumber();
        if (phoneNumber == null || phoneNumber.length() == 0) throw new ReservationException("휴대폰 번호가 없는 예약입니다. (홈페이지 예약이 아님)");
    }

    public List<MonthlyStatisticDto> getMonthlyStatistic(int year, int month) {
        List<MonthlyStatisticDto> monthlyStatisticDtoList = new ArrayList<>();
        List<RoomInfoDto> roomInfoDtos = roomService.showAllRooms();
        for (RoomInfoDto roomInfoDto:roomInfoDtos) {
            MonthlyStatisticDto monthlyStatisticDto = new MonthlyStatisticDto(year, month, roomInfoDto.getRoomId());
            monthlyStatisticDtoList.add(monthlyStatisticDto);

            List<Reservation> reservationList = reservationRepository.findAllByRoomIdAndDateBetweenAndReservationState(roomInfoDto.getRoomId(), LocalDate.of(year, month, 1), LocalDate.of(year, month, 31), 1);

            MonthlyStatisticOriginDto homepage_reservations = new MonthlyStatisticOriginDto("GuestHome");
            MonthlyStatisticOriginDto airbnb_reservations = new MonthlyStatisticOriginDto("GuestAirbnb");
            MonthlyStatisticOriginDto booking_reservations = new MonthlyStatisticOriginDto("GuestBooking");

            for (Reservation reservation:reservationList) {
                switch (reservation.getReservedFrom()) {
                    case "GuestHome":
                        homepage_reservations.addReservationCount();
                        break;
                    case "GuestAirbnb":
                        airbnb_reservations.addReservationCount();
                        break;
                    case "GuestBooking":
                        booking_reservations.addReservationCount();
                        break;
                    default:
                        log.error("예약 출처 에러 - {}", reservation.getReservedFrom());
                        break;
                }
            }
        }
        return monthlyStatisticDtoList;
    }

}
