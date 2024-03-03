package com.yeoyeo.adapter.controller;

import com.yeoyeo.application.common.dto.GeneralResponseDto;
import com.yeoyeo.application.common.etc.ScheduleManage;
import com.yeoyeo.application.common.method.CommonMethod;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스케쥴 실행 API", description = "Redis 장애시 SingleJob 처리용도")
@RequiredArgsConstructor
@RestController
@RequestMapping("schedule")
public class ScheduleController {

	@Value("schedule.key")
	String SCHEDULE_KEY;

	private final CommonMethod commonMethod;
	private final ScheduleManage scheduleManage;

	@PostMapping("/run/{scheduleName}")
	public ResponseEntity<GeneralResponseDto> fallbackRun(@PathVariable String scheduleName, @RequestHeader("schedule-token") String token) {
		if (token.equals(commonMethod.encodeToken(SCHEDULE_KEY, scheduleName))) {
			scheduleManage.runSchedule(scheduleName);
			return ResponseEntity.status(HttpStatus.OK).body(GeneralResponseDto.builder().success(true).message("Schedule request succeed").build());
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GeneralResponseDto.builder().success(false).message("Invalid Token").build());
	}

}
