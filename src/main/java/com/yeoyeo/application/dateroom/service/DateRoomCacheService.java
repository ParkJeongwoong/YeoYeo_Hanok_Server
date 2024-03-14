package com.yeoyeo.application.dateroom.service;

import com.yeoyeo.application.common.exception.AsyncApiException;
import com.yeoyeo.application.dateroom.dto.DateRoom2MonthDto;
import com.yeoyeo.application.dateroom.dto.DateRoomCacheDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoByDateDto;
import com.yeoyeo.application.dateroom.dto.DateRoomInfoDto;
import com.yeoyeo.application.message.service.MessageService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DateRoomCacheService {

	private final MessageService messageService;
	private final DateRoomInfoService dateRoomInfoService;
	private final RedisTemplate<String, Object> redisTemplate;

	public DateRoom2MonthDto getCachedDateRoomInfo() throws RedisConnectionFailureException  {
		LocalDate now = LocalDate.now();
		int year = now.getYear();
		int month = now.getMonthValue();
		LocalDate next = now.plusMonths(1);
		List<DateRoomInfoByDateDto> thisMonth = getOrSetCachedDateRoomInfoList(year, month);
		List<DateRoomInfoByDateDto> nextMonth = getOrSetCachedDateRoomInfoList(next.getYear(), next.getMonthValue());
		return new DateRoom2MonthDto(thisMonth, nextMonth);
	}

	private List<DateRoomInfoByDateDto> getOrSetCachedDateRoomInfoList(int year, int month) throws RedisConnectionFailureException {
		List<DateRoomInfoByDateDto> dateRoomInfoByDateDtos = new ArrayList<>();
		// 여유 방 데이터
		Map<String, DateRoomInfoDto> entries1 = getDateRoomCache(year, month, 1);
		if (entries1.isEmpty()) {
			log.info("CACHE MISS (여유 방 데이터)");
			return setCachedDateRoomInfoList(year, month);
		}

		// 여행 방 데이터
		Map<String, DateRoomInfoDto> entries2 = getDateRoomCache(year, month, 2);
		if (entries2.isEmpty()) {
			log.info("CACHE MISS (여행 방 데이터)");
			return setCachedDateRoomInfoList(year, month);
		}

		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.plusMonths(1);
		for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
			String dateStr = date.toString();
			DateRoomInfoByDateDto dateRoomInfoByDateDto = new DateRoomInfoByDateDto(date, entries1.get(dateStr));
			if (entries2.containsKey(dateStr)) dateRoomInfoByDateDto.addDateRoomInfo(entries2.get(dateStr));
			dateRoomInfoByDateDtos.add(dateRoomInfoByDateDto);
		}

		return dateRoomInfoByDateDtos;
	}

	private Map<String, DateRoomInfoDto> getDateRoomCache(int year, int month, int type) throws RedisConnectionFailureException {
		HashOperations<String, String, DateRoomInfoDto> hashOperations = redisTemplate.opsForHash();
		String key = getDateRoomCacheKey(year, month, type);
		return hashOperations.entries(key);
	}

	private List<DateRoomInfoByDateDto> setCachedDateRoomInfoList(int year, int month) throws RedisConnectionFailureException {
		HashOperations<String, String, DateRoomInfoDto> hashOperations = redisTemplate.opsForHash();
		LocalDate startDate = LocalDate.of(year, month, 1);
		List<DateRoomInfoByDateDto> dateRoomInfoByDateDtos = dateRoomInfoService.getDateRoomInfoListByDate(startDate);
		String key1 = getDateRoomCacheKey(year, month, 1);
		String key2 = getDateRoomCacheKey(year, month, 2);
		log.info("key1: {} year: {} month: {}", key1, year, month);
		log.info("key2: {} year: {} month: {}", key2, year, month);

		dateRoomInfoByDateDtos.forEach(dto -> {
			String dateStr = dto.getDate().toString();
			dto.getRooms().forEach(dateRoomInfoDto -> {
				if (dateRoomInfoDto.getRoomName().equals("여유")) {
					hashOperations.put(key1, dateStr, dateRoomInfoDto);
				}
				else {
					hashOperations.put(key2, dateStr, dateRoomInfoDto);
				}
			});
		});

		return dateRoomInfoByDateDtos;
	}

	private String getDateRoomCacheKey(int year, int month, long roomId) {
		return year + "-" + month + ":" + roomId;
	}

	@Retryable(retryFor = {AsyncApiException.class}, maxAttempts = 5, backoff = @Backoff(random = true, delay = 1000, maxDelay = 3000))
	@Async("redisExecutor")
	public void updateCache(DateRoomCacheDto cacheDto) {
		try {
			HashOperations<String, String, DateRoomInfoDto> hashOperations = redisTemplate.opsForHash();
			String key = getDateRoomCacheKey(cacheDto.getDate().getYear(),
				cacheDto.getDate().getMonthValue(), cacheDto.getRoomId());
			String hashKey = cacheDto.getDate().toString();
			// 해당 캐시 데이터가 있으면 업데이트, 없으면 Skip
			log.info("캐시 업데이트 시도: {} {}", key, hashKey);
			if (Boolean.TRUE.equals(hashOperations.hasKey(key, hashKey))) {
				log.info("Key 존재");
				hashOperations.put(key, hashKey, new DateRoomInfoDto(cacheDto));
				log.info("캐시 업데이트: {} {}", key, hashKey);
			} else {
				log.info("캐시 업데이트 Skip: {} {}", key, hashKey);
			}
		} catch (RedisConnectionFailureException e) {
			log.error("Redis 연결 실패: {}", e.getMessage());
			messageService.sendDevMsg("DateRoom 캐시 업데이트 중 Redis 연결 실패: " + e.getMessage());
			throw new AsyncApiException("캐시 업데이트 비동기 작업 실패 : Dateroom ID " + cacheDto.getDateRoomId(), e);
		} catch (Exception e) {
			log.error("Dateroom {} - 캐시 업데이트 실패: {}", cacheDto.getDateRoomId(), e.getMessage());
			throw new AsyncApiException("캐시 업데이트 비동기 작업 실패 : Dateroom ID " + cacheDto.getDateRoomId(), e);
		}
	}
	@Recover
	public void recover(AsyncApiException e) {
		log.error("캐시 업데이트 비동기 작업 실패 후 Recover: {}", e.getMessage());
		messageService.sendDevMsg("캐시 업데이트 비동기 작업 실패: " + e.getMessage());
	}

}
