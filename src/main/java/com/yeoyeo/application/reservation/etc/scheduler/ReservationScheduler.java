package com.yeoyeo.application.reservation.etc.scheduler;

import com.yeoyeo.application.admin.repository.AdminManageInfoRepository;
import com.yeoyeo.application.admin.service.AdminManageService;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.Admin.AdminManageInfo;
import com.yeoyeo.domain.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationScheduler {

    private final MessageService messageService;
    private final AdminManageService adminManageService;
    private final ReservationRepository reservationRepository;
    private final AdminManageInfoRepository adminManageInfoRepository;

    @Transactional
    @PostConstruct
    private void init() {
        dailyReservationCompletion();
    }

    @Transactional
    @Scheduled(cron = "0 1 0 * * *") // 매일 0시 1분 0초 동작
    protected void dailyReservationCompletion() {
        log.info("[SCHEDULE - Daily Past Reservation Completion]");
        LocalDate today = LocalDate.now();
        log.info("{} 예약 완료 처리 시작", today);
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        log.info("대기 중인 예약 건수 : {}건", reservationList.size());
        try {
            for (Reservation reservation : reservationList) {
                if (reservation.getFirstDateRoom().getDate().isBefore(today)) reservation.setStateComplete();
                else break;
            }
            reservationRepository.saveAll(reservationList);
        } catch (ReservationException reservationException) {
            log.error("예약 완료 처리 중 오류 발생", reservationException);
        }
        log.info("예약 완료 처리 정상 종료");
    }

    @Transactional
    @Scheduled(cron = "0 1 3 * * *") // 매일 3시 1분 0초 동작
    public void dailyReservationClearing() {
        log.info("[SCHEDULE - Daily Unpaid Reservation Clearing]");
        LocalDateTime before24hour = LocalDateTime.now().minusDays(1);
        log.info("{} 시점 기준 미결제 예약 삭제 처리 시작(24시간 전)", before24hour);
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(0).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        log.info("미결제 예약 건수 : {}건", reservationList.size());
        int deletedCnt = 0;
        for (Reservation reservation : reservationList) {
            if (reservation.getFirstDateRoom()==null) reservationRepository.delete(reservation);
            else if (reservation.getCreatedDate().isBefore(before24hour)) reservationRepository.delete(reservation);
            else break;
            deletedCnt += 1;
        }
        log.info("미결제 예약 삭제 건수 : {}건", deletedCnt);
        log.info("미결제 예약 삭제 처리 정상 종료");
    }

    @Transactional
    @Scheduled(cron = "0 0 5 * * *") // 매일 5시 0분 0초 동작
    public void dailyAdminManageInfoCreate() {
        log.info("[SCHEDULE = Daily AdminManageInfo Creation]");
        adminManageService.createAdminManageInfoList();
    }

    @Scheduled(cron = "0 0 8 * * *") // 매일 8시 0분 0초 동작
    public void noticeMessage_BeforeCheckIn() {
        log.info("[SCHEDULE - Sending Notice Message - Before Check-in]");
        LocalDate today = LocalDate.now();
        log.info("{} 체크인 고객 문자 발송", today);
        // 홈페이지 예약 고객
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        int cnt = 0;
        for (Reservation reservation : reservationList) {
            if (reservation.getFirstDate().isEqual(today)) {
                if (validateManagingCondition(reservation)) messageService.sendCheckInMsg(reservation.getGuest().getNumberOnlyPhoneNumber());
                cnt += 1;
            }
            else break;
        }
        // 전화 예약 고객
        List<AdminManageInfo> adminManageInfoList = adminManageInfoRepository.findAllByCheckinAndActivated(today, true);
        for (AdminManageInfo adminManageInfo : adminManageInfoList) {
            if (adminManageInfo.getGuestType() == 2 && adminManageInfo.getPhoneNumber() != null) {
                messageService.sendCheckInMsg(adminManageInfo.getNumberOnlyPhoneNumber());
            }
        }
        log.info("금일 체크인 고객 수 : {}건", cnt);
        log.info("금일 체크인 고객 문자 전송 정상 종료");
    }

    @Scheduled(cron = "0 20 15 * * *") // 매일 15시 20분 0초 동작
    public void noticeMessage_AfterCheckIn() {
        log.info("[SCHEDULE - Sending Notice Message - After Check-in]");
        LocalDate today = LocalDate.now();
        log.info("{} 체크인 후 안내 문자 발송", today);
        // 홈페이지 예약 고객
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        for (Reservation reservation : reservationList) {
            if (reservation.getFirstDate().isEqual(today)) {
                if (validateManagingCondition(reservation)) messageService.sendAfterCheckInMsg(reservation.getGuest().getNumberOnlyPhoneNumber());
            }
            else break;
        }
        // 전화 예약 고객
        List<AdminManageInfo> adminManageInfoList = adminManageInfoRepository.findAllByCheckinAndActivated(today, true);
        for (AdminManageInfo adminManageInfo : adminManageInfoList) {
            if (adminManageInfo.getGuestType() == 2 && adminManageInfo.getPhoneNumber() != null) {
                messageService.sendAfterCheckInMsg(adminManageInfo.getNumberOnlyPhoneNumber());
            }
        }
        log.info("체크인 후 안내 문자 전송 정상 종료");
    }

    @Transactional
    @Scheduled(cron = "10 0 20 * * *") // 매일 20시 0분 10초 동작
    public void dailyAdminCheckInNotice() {
        log.info("[SCHEDULE - Daily Admin Check-in info Notice]");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("대상 날짜 : {}", tomorrow);
        List<AdminManageInfo> adminManageInfos = adminManageInfoRepository.findAllByCheckinAndActivated(tomorrow, true)
                .stream().filter(adminManageInfo -> adminManageInfo.getGuestType() != 1).collect(Collectors.toList());
        log.info("익일 체크인 건수 : {}건", adminManageInfos.size());
        messageService.sendAdminCheckInMsg(adminManageInfos);
        log.info("익일 체크인 정보 문자 전송 정상 종료");
    }

    @Transactional
    @Scheduled(cron = "0 30 23 * * *") // 매일 23시 30분 0초 동작
    public void dailyAdminMangeInfoDeactivate() {
        log.info("[SCHEDULE - Daily AdminManageInfo Deactivate]");
        LocalDate today = LocalDate.now();
        log.info("체크아웃 대상 날짜 : {}", today);
        List<AdminManageInfo> adminManageInfos = adminManageInfoRepository.findAllByCheckoutAndActivated(today, true);
        log.info("체크아웃 건수 : {}건", adminManageInfos.size());
        for (AdminManageInfo adminManageInfo : adminManageInfos) adminManageInfo.setActivated(false);
        adminManageInfoRepository.saveAll(adminManageInfos);
        log.info("체크인 된 호스트 관리 예약 비활성화 처리 정상 종료");
    }

    private boolean validateManagingCondition(Reservation reservation) {
        if (reservation.getGuest().getPhoneNumber() == null) return false;
        if (reservation.getManagementLevel() == 0) return false;
        return !reservation.getGuest().getName().equals("AirBnbGuest");
    }

}
