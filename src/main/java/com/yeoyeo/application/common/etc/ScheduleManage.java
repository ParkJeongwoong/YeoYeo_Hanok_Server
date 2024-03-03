package com.yeoyeo.application.common.etc;

import com.yeoyeo.application.common.method.CommonMethod;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScheduleManage {

	private static final Map<String,String[]> SCHEDULE_NAMES = Map.of(
		"dateRoomGenerator", new String[] {
			"dailyRoomReservableJob", // 매일 0시 0분 3초 동작
			"dailyRoomCreation", // 매일 0시 0분 10초 동작
			"weeklyDefaultPriceTypeReset", // 매주 월요일 새벽 3시에 동작
			"dailyRoomUnReservableJob" // 매일 5시 30븐 0초 동작
		},
		"calendarScheduler", new String[] {
			"regularSync_Airbnb" // 3시간마다 도는 스케줄러
		},
		"reservationScheduler", new String[] {
			"dailyReservationCompletion", // 매일 0시 0분 0초 동작
			"dailyReservationClearing", // 매일 3시 1분 0초 동작
			"dailyAdminManageInfoCreate", // 매일 5시 0분 0초 동작
			"noticeMessage_BeforeCheckIn", // 매일 10시 0분 0초 동작
			"noticeMessage_AfterCheckIn", // 매일 15시 20분 0초 동작
			"dailyAdminCheckInNotice", // 매일 20시 0분 10초 동작
			"noticeBefore7days", // 매일 20시 30분 10초 동작
			"dailyAdminManageInfoDeactivate" // 매일 23시 30분 0초 동작
		}
	);

	private final RedisTemplate<String, String> redisTemplate;
	private final CommonMethod commonMethod;
	private final ApplicationContext applicationContext;

	@EventListener(ContextRefreshedEvent.class)
	private synchronized void checkRunningSchedule() {
		log.info("[Check Running Schedule]");
		String serverName = commonMethod.getServerProfile();
		SCHEDULE_NAMES.forEach((serviceName, scheduleNameList) -> {
			for (String scheduleName : scheduleNameList) {
				String runningServer = redisTemplate.opsForValue().get(scheduleName);
				if (runningServer != null && runningServer.equals(serverName)) {
					log.info("Redo Scheduling : {}", scheduleName);
					redisTemplate.delete(scheduleName);
					runSchedule(serviceName, scheduleName);
				}
			}
		});
	}

	private void runSchedule(String serviceName, String scheduleName) {
		Scheduler bean = applicationContext.getBean(serviceName, Scheduler.class);
		try {
			log.info("Redo Scheduling : {} - {}", serviceName, scheduleName);
			bean.getClass().getMethod(scheduleName).invoke(bean);
		} catch (Exception e) {
			log.error("Redo Scheduling Error : {}", e.getMessage());
		}
	}

	public void runSchedule(String schedule) throws SchedulingException {
		SCHEDULE_NAMES.forEach((serviceName, scheduleNameList) -> {
			for (String scheduleName : scheduleNameList) {
				if (schedule.equals(scheduleName)) {
					log.info("Schedule Found - {}", schedule);
					runSchedule(serviceName, scheduleName);
					return;
				}
			}
		});
		log.error("Schedule NOT FOUND = {}", schedule);
		throw new SchedulingException("Schedule NOT FOUND");
	}

}
