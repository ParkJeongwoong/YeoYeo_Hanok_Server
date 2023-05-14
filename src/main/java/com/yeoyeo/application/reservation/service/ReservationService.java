package com.yeoyeo.application.reservation.service;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.dateroom.etc.exception.RoomReservationException;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.dto.MakeReservationAdminRequestDto;
import com.yeoyeo.application.reservation.dto.MakeReservationDto;
import com.yeoyeo.application.reservation.dto.ReservationDetailInfoDto;
import com.yeoyeo.application.reservation.dto.ReservationInfoDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Guest;
import com.yeoyeo.domain.Payment;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReservationService {

    private final DateRoomRepository dateRoomRepository;
    private final ReservationRepository reservationRepository;
    private final MessageService messageService;

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
        if (dateRoomList.size() == 0) throw new ReservationException("존재하지 않는 방입니다.");
        for (DateRoom dateRoom:dateRoomList) if (dateRoom.getRoomReservationState() != 0) throw new ReservationException("이미 예약이 완료된 방입니다.");
        Guest guest = reservationDto.getGuest();
        Reservation reservation = Reservation.builder()
                .dateRoomList(dateRoomList)
                .guest(guest)
                .managementLevel(reservationDto.getManagement_level())
                .build();
        reservationRepository.save(reservation);
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
//        List<DateRoom> dateRoomList = reservation.getDateRoomList();
        List<String> dateRoomIdList = reservation.getDateRoomIdList();
        try {
//            for (DateRoom dateRoom:dateRoomList) {
            for (String dateRoomId:dateRoomIdList) {
                DateRoom dateRoom = dateRoomRepository.findById(dateRoomId).orElseThrow(NoSuchElementException::new);
                log.info("{} : {} {} 예약시도", reservation.getGuest().getName(), dateRoom.getDate(), dateRoom.getRoom().getId());
                dateRoom.setStateBooked();
                dateRoomRepository.save(dateRoom);
//                dateRoomRepository.saveAndFlush(dateRoom); // 이렇게 해야 메서드 내에서 ObjectOptimisticLockingFailureException 발생
                log.info("{} : {} {} 예약성공", reservation.getGuest().getName(), dateRoom.getDate(), dateRoom.getRoom().getId());
            }
            reservation.setPayment(payment);
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
            for (DateRoom dateRoom:dateRoomList) dateRoom.resetState();
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
            for (DateRoom dateRoom:reservation.getDateRoomList()) dateRoom.resetState();
            reservationRepository.save(reservation);
            log.info("{} 고객님의 예약이 취소되었습니다.", reservation.getGuest().getName());
        } catch (ReservationException reservationException) {
            log.error("Reservation 상태 변경 에러", reservationException);
            throw new ReservationException(reservationException.getMessage());
        }
    }

    // PhoneNumber가 없는 에어비앤비 예약 필터링
    private void checkPhoneNumber(Reservation reservation) throws ReservationException {
        String phoneNumber = reservation.getGuest().getPhoneNumber();
        if (phoneNumber == null || phoneNumber.length() == 0) throw new ReservationException("휴대폰 번호가 없는 예약입니다. (홈페이지 예약이 아님)");
    }

}
