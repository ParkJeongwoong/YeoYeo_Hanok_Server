package com.yeoyeo.application.admin.service;

import com.yeoyeo.application.admin.dto.AdminManageInfoRequestDto;
import com.yeoyeo.application.admin.dto.AdminManageInfoResponseDto;
import com.yeoyeo.application.admin.etc.exception.AdminManageInfoException;
import com.yeoyeo.application.admin.repository.AdminManageInfoRepository;
import com.yeoyeo.application.dateroom.repository.DateRoomRepository;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.application.room.repository.RoomRepository;
import com.yeoyeo.domain.Admin.AdminManageInfo;
import com.yeoyeo.domain.DateRoom;
import com.yeoyeo.domain.Reservation;
import com.yeoyeo.domain.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminManageService {

    private final AdminManageInfoRepository adminManageInfoRepository;
    private final ReservationRepository reservationRepository;
    private final DateRoomRepository dateRoomRepository;
    private final RoomRepository roomRepository;

    public List<AdminManageInfoResponseDto> getAdminManageInfoList() {
        return adminManageInfoRepository.findAllByOrderByCheckinAscRoom_Id()
                .stream().map(AdminManageInfo::makeAdminManageInfoResponseDto).collect(Collectors.toList());
    }

    public List<AdminManageInfoResponseDto> getAdminManageInfoList(LocalDate checkOut) {
        return adminManageInfoRepository.findAllByCheckoutGreaterThanOrderByCheckinAscRoom_Id(checkOut)
                .stream().filter(AdminManageInfo::isActivated)
                .map(AdminManageInfo::makeAdminManageInfoResponseDto).collect(Collectors.toList());
    }

    @Transactional
    public void createAdminManageInfoList() {
        List<AdminManageInfo> adminManageInfoList = new ArrayList<>();
        List<Reservation> reservations = reservationRepository.findAllByReservationState(1);
        log.info("RESERVATION COUNT : {}", reservations.size());
        for (Reservation reservation : reservations) {
            AdminManageInfo adminManageInfo = adminManageInfoRepository.findByCheckinAndRoom_IdAndActivated(reservation.getFirstDate(), reservation.getRoom().getId(), true);
            log.info("ADMIN MANGE INFO : {}", adminManageInfo);
            if (adminManageInfo == null) adminManageInfoList.add(new AdminManageInfo(reservation));
            else if (reservation.getGuest().getName().equals("AirBnbGuest_External")) {
                continue;
            }
            else if (adminManageInfo.getReservation().getId() != reservation.getId()) {
                adminManageInfo.setActivated(false);
                adminManageInfoList.add(new AdminManageInfo(reservation));
            }
        }
        log.info("ADMIN MANAGE INFO COUNT : {}", adminManageInfoList.size());

        List<AdminManageInfo> notReservedList = getNotReservedInfo();
        log.info("NOT RESERVED COUNT : {}", notReservedList.size());
        for (AdminManageInfo notReservedInfo : notReservedList) {
            notReservedInfo.setActivated(false);
        }

        adminManageInfoRepository.saveAll(adminManageInfoList);
        adminManageInfoRepository.saveAll(notReservedList);
    }

    @Transactional
    public void addAdminManageInfo(AdminManageInfoRequestDto requestDto) throws AdminManageInfoException {
        List<DateRoom> dateRoomList = dateRoomRepository.findAllByDateBetweenAndRoom_Id(requestDto.getCheckIn(), requestDto.getCheckOut().minusDays(1), requestDto.getRoomId());
        for (DateRoom dateRoom : dateRoomList) {
            if (dateRoom.getRoomReservationState() != 1) {
                log.info("예약되지 않은 날짜입니다.");
                return;
            }
        }
        AdminManageInfo existingEntity = adminManageInfoRepository.findByCheckinAndRoom_IdAndActivated(requestDto.getCheckIn(), requestDto.getRoomId(), true);
        if (existingEntity != null) throw new AdminManageInfoException("이미 해당 날짜에 AdminManageInfo 가 존재합니다.");

        Room room = roomRepository.findById(requestDto.getRoomId()).orElseThrow(()->new NoSuchElementException("Room ID 가 잘못되었습니다."));
        Reservation reservation = reservationRepository.findById(requestDto.getReservationId()).orElseThrow(()->new NoSuchElementException("Reservation ID 가 잘못되었습니다."));
        AdminManageInfo adminManageInfo = AdminManageInfo.builder()
                .guestType(2).checkin(requestDto.getCheckIn()).checkout(requestDto.getCheckOut()).room(room)
                .name(requestDto.getGuestName()).phoneNumber(requestDto.getGuestPhoneNumber()).guestCount(requestDto.getGuestCount())
                .request(requestDto.getRequest())
                .reservation(reservation)
                .build();
        adminManageInfoRepository.save(adminManageInfo);
    }

    @Transactional
    public void setAdminManageInfo(AdminManageInfoRequestDto requestDto) throws NoSuchElementException {
        AdminManageInfo adminManageInfo = adminManageInfoRepository.findByCheckinAndRoom_IdAndActivated(requestDto.getCheckIn(), requestDto.getRoomId(), true);
        if (adminManageInfo == null) throw new NoSuchElementException("해당 AdminManageInfo는 존재하지 않습니다.");
        adminManageInfo.setCheckout(requestDto.getCheckOut());
        adminManageInfo.setName(requestDto.getGuestName());
        adminManageInfo.setPhoneNumber(requestDto.getGuestPhoneNumber());
        adminManageInfo.setGuestCount(requestDto.getGuestCount());
        adminManageInfo.setRequest(requestDto.getRequest());
        adminManageInfoRepository.save(adminManageInfo);
    }

    @Transactional
    public void deactivateAdminManageInfo(AdminManageInfoRequestDto requestDto) throws NoSuchElementException {
        log.info("{} {}", requestDto.getCheckIn(), requestDto.getRoomId());
        AdminManageInfo adminManageInfo = adminManageInfoRepository.findByCheckinAndRoom_IdAndActivated(requestDto.getCheckIn(), requestDto.getRoomId(), true);
        if (adminManageInfo == null) throw new NoSuchElementException("해당 AdminManageInfo는 존재하지 않습니다.");
        adminManageInfo.setActivated(false);
    }

    private List<AdminManageInfo> getNotReservedInfo() {
        return adminManageInfoRepository.findAllByCheckoutGreaterThanOrderByCheckinAscRoom_Id(LocalDate.now())
                .stream().filter(adminManageInfo -> {
                    DateRoom dateRoom = dateRoomRepository.findById(adminManageInfo.getCheckin().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+adminManageInfo.getRoom().getId()).orElse(null);
                    return dateRoom == null || dateRoom.isReservable() == false || dateRoom.getRoomReservationState() == 0;
                }).collect(Collectors.toList());
    }


}
