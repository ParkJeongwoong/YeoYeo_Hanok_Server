package com.yeoyeo.application.reservation.etc.scheduler;

import com.yeoyeo.application.admin.repository.AdminManageInfoRepository;
import com.yeoyeo.application.admin.service.AdminManageService;
import com.yeoyeo.application.common.etc.Scheduler;
import com.yeoyeo.application.message.dto.SendMessageResponseDto;
import com.yeoyeo.application.message.service.MessageService;
import com.yeoyeo.application.reservation.dto.SendAdminCheckInMsgDto;
import com.yeoyeo.application.reservation.etc.exception.ReservationException;
import com.yeoyeo.application.reservation.repository.ReservationRepository;
import com.yeoyeo.domain.Admin.AdminManageInfo;
import com.yeoyeo.domain.Reservation;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationScheduler extends Scheduler {

    private final MessageService messageService;
    private final AdminManageService adminManageService;
    private final ReservationRepository reservationRepository;
    private final AdminManageInfoRepository adminManageInfoRepository;

    @Transactional
    @PostConstruct
    public void init() {
        dailyReservationCompletion();
    }

    @Transactional
    @Scheduled(cron = "0 1 0 * * *") // 매일 0시 1분 0초 동작
    public synchronized void dailyReservationCompletion() {
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

    /* [문제 상황] - 2024-05-10
    결제까지 마친 고객의 예약정보가 사라지는 문제 발생.
    검증이 필요하지만 Reservation이 삭제되어 예약 상태 확인이 불가능한 상황.
    Reservation을 삭제하는 유일한 로직이 아래의 스케줄러이기 때문에 여기서 문제가 발생한 것으로 추정됨.
    [해결 방안]
    Payment가 존재하는 Reservation은 삭제하지 않도록 수정.
     */
    @Transactional
    @Scheduled(cron = "0 1 3 * * *") // 매일 3시 1분 0초 동작
    public synchronized void dailyReservationClearing() {
        log.info("[SCHEDULE - Daily Unpaid Reservation Clearing]");
        LocalDateTime before24hour = LocalDateTime.now().minusDays(1);
        log.info("{} 시점 기준 미결제 예약 삭제 처리 시작(24시간 전)", before24hour);
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(0).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        log.info("미결제 예약 건수 : {}건", reservationList.size());
        int deletedCnt = 0;
        for (Reservation reservation : reservationList) {
            if (reservation.getPayment()!=null) continue;
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
    public synchronized void dailyAdminManageInfoCreate() {
        log.info("[SCHEDULE = Daily AdminManageInfo Creation]");
        adminManageService.createAdminManageInfoList();
    }

    @Scheduled(cron = "0 0 10 * * *") // 매일 10시 0분 0초 동작
    public synchronized void noticeMessage_BeforeCheckIn() {
        log.info("[SCHEDULE - Sending Notice Message - Before Check-in]");
        LocalDate today = LocalDate.now();
        log.info("{} 체크인 고객 문자 발송", today);
        // 홈페이지 예약 고객
        List<Reservation> reservationList = reservationRepository.findAllByReservationState(1).stream().sorted(Comparator.comparing(Reservation::getFirstDate)).collect(Collectors.toList());
        int cnt = 0;
        for (Reservation reservation : reservationList) {
            if (reservation.getFirstDate().isEqual(today)) {
                if (validateManagingCondition(reservation)) {
                    SendMessageResponseDto responseDto = messageService.sendCheckInMsg(reservation.getGuest().getNumberOnlyPhoneNumber(), reservation.getRoom().getName());
                    log.info("문자 발송 결과 : {}", responseDto.toString());
                }
                cnt += 1;
            }
            else break;
        }
        // 전화 예약 고객
        List<AdminManageInfo> adminManageInfoList = adminManageInfoRepository.findAllByCheckinAndActivated(today, true);
        for (AdminManageInfo adminManageInfo : adminManageInfoList) {
            if (adminManageInfo.getGuestType() == 2 && adminManageInfo.getPhoneNumber() != null) {
                SendMessageResponseDto responseDto = messageService.sendCheckInMsg(adminManageInfo.getNumberOnlyPhoneNumber(), adminManageInfo.getRoom().getName());
                log.info("문자 발송 결과 : {}", responseDto.toString());
            }
        }
        log.info("금일 체크인 고객 수 : {}건", cnt);
        log.info("금일 체크인 고객 문자 전송 정상 종료");
    }

    @Scheduled(cron = "0 20 15 * * *") // 매일 15시 20분 0초 동작
    public synchronized void noticeMessage_AfterCheckIn() {
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
    public synchronized void dailyAdminCheckInNotice() {
        log.info("[SCHEDULE - Daily Admin Check-in info Notice]");
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("대상 날짜 : {}", tomorrow);
        List<AdminManageInfo> adminManageInfos = adminManageInfoRepository.findAllByCheckinAndActivated(tomorrow, true);
        log.info("익일 체크인 건수 : {}건", adminManageInfos.size());
        messageService.sendAdminCheckInMsg(new SendAdminCheckInMsgDto(adminManageInfos));
        log.info("익일 체크인 정보 문자 전송 정상 종료");
    }

    @Transactional
    @Scheduled(cron = "10 30 20 * * *") // 매일 20시 30분 10초 동작
    public synchronized void noticeBefore7days() {
        log.info("[SCHEDULE - Notice Before 7 Days of Check-in]");
        LocalDate after7days = LocalDate.now().plusDays(7);
        log.info("대상 날짜 : {}", after7days);
        List<AdminManageInfo> adminManageInfos = adminManageInfoRepository.findAllByCheckinAndActivated(after7days, true);
        for (AdminManageInfo adminManageInfo : adminManageInfos) {
            if (adminManageInfo.getPhoneNumber() != null) {
                SendMessageResponseDto responseDto = messageService.sendNotice7DaysBeforeMsg(adminManageInfo.getNumberOnlyPhoneNumber());
                log.info("문자 발송 결과 : {}", responseDto.toString());
            }
        }
        log.info("체크인 7일 전 안내 문자 전송 정상 종료");
    }

    @Transactional
    @Scheduled(cron = "0 30 23 * * *") // 매일 23시 30분 0초 동작
    public synchronized void dailyAdminManageInfoDeactivate() {
        log.info("[SCHEDULE - Daily AdminManageInfo Deactivate]");
        LocalDate before2days = LocalDate.now().minusDays(2);
        log.info("체크아웃 대상 날짜 : {}", before2days);
        List<AdminManageInfo> adminManageInfos = adminManageInfoRepository.findAllByCheckoutAndActivated(before2days, true);
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

//    @Scheduled(cron = "0 30 10 * * *") // 매일 10시 30분 0초 동작
    public synchronized void nudgeNaverReview() {
        log.info("[SCHEDULE - Nudge Naver Review]");
        LocalDate checkOutDay = LocalDate.now().minusDays(1);
        LocalDate lastDay = checkOutDay.minusDays(1);
        log.info("오늘 날짜 : {}, 체크아웃 날짜 : {}, 마지막 숙박 날짜 : {}", LocalDate.now(), checkOutDay, lastDay);
        List<Reservation> yesterdayCheckOutReservationList = reservationRepository.findAllByLastDateAndReservedFrom(lastDay, "GuestNaver");
        List<Reservation> yesterdayStayReservationList = reservationRepository.findAllByLastDateAndReservedFrom(checkOutDay, "GuestNaver");
        List<Long> yesterdayStayReservationIDList = yesterdayStayReservationList.stream().map(Reservation::getId).collect(Collectors.toList());

        log.info("네이버 리뷰 요청 대상 건수 : {}건", yesterdayCheckOutReservationList.size());
        for (Reservation reservation : yesterdayCheckOutReservationList) {
            if (yesterdayStayReservationIDList.contains(reservation.getId())) continue;
            if (reservation.getGuest().getPhoneNumber() != null) {
                log.info("네이버 리뷰 요청 문자 발송 대상 : {}", reservation.getGuest().getName());
                SendMessageResponseDto responseDto = messageService.sendNaverReviewNudge(reservation.getGuest().getNumberOnlyPhoneNumber());
                log.info("문자 발송 결과 : {}", responseDto.toString());
            }
        }
        log.info("네이버 리뷰 요청 문자 전송 정상 종료");
    }

}
