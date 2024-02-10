package com.yeoyeo.aop;

import com.yeoyeo.application.common.method.CommonMethod;
import com.yeoyeo.application.reservation.etc.scheduler.ReservationScheduler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SchedulerAopTest {

    @Autowired
    ReservationScheduler reservationScheduler;

    @Autowired
    CommonMethod commonMethod;

    public static void fail(String message) {
        if (message == null) {
            throw new AssertionError();
        }
        throw new AssertionError(message);
    }

    @Test
    void dailyReservationCompletionFailTest() {
        log.info("SchedulerAopTest.dailyReservationCompletionFailTest");
        commonMethod.setCache("dailyReservationCompletion", "testing");

        reservationScheduler.dailyReservationCompletion();

        String cacheValue = commonMethod.getCache("dailyReservationCompletion");
        String profile = commonMethod.getServerProfile();
        if (cacheValue.equals(profile)) {
            fail("TEST FAILED");
        } else if (cacheValue.equals("testing")) {
            log.info("TEST SUCCESS");
        } else {
            fail("TEST FAILED (UNEXPECTED CACHE VALUE)");
        }
        commonMethod.delCache("dailyReservationCompletion");
    }

    @Test
    void dailyReservationCompletionTest() {
        log.info("SchedulerAopTest.dailyReservationCompletionTest");
        reservationScheduler.dailyReservationCompletion();
    }

}
