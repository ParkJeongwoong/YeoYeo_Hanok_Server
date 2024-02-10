package com.yeoyeo.application.common.method;

import com.yeoyeo.application.common.exception.SchedulingException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommonMethod {

    private final RedisTemplate<String, String> redisTemplate;
    private final Environment env;

    public void printIp(String from) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getHeader("X-FORWARDED-FOR");
        log.info("from {} - X-FORWARDED-FOR : {}", from, ip);
        if (ip == null) {
            ip = request.getRemoteAddr();
            log.info("from {} - getRemoteAddr: {}", from, ip);
        }
    }

    public void setCache(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    public void setCache(String key, String value, int expireMinutes) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value, expireMinutes, TimeUnit.MINUTES);
    }

    public String getCache(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    // Todo : Cache Hit or Miss 추가
    // Key와 repository를 받아서 cache hit or miss를 확인하고, hit이면 repository를 호출하지 않고, miss이면 repository를 호출하고 cache에 저장한다.

    public boolean delCache(String key) {
        return redisTemplate.delete(key);
    }

    public void startScheduling(String schedulingName) throws SchedulingException {
        String serverName = getServerProfile();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if (Boolean.FALSE.equals(valueOperations.setIfAbsent(schedulingName, serverName, 1, TimeUnit.HOURS))) {
            throw new SchedulingException("Scheduling is already started");
        }
    }

    public void endScheduling(String schedulingName) throws SchedulingException {
        String serverName = getServerProfile();
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        if (valueOperations.get(schedulingName) == null ||
            !Objects.equals(valueOperations.get(schedulingName), serverName)) {
            throw new SchedulingException("Scheduling is not started or not started by this server");
        } else {
            delCache(schedulingName);
        }
    }

    public String getServerProfile() {
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        List<String> realProfiles = Arrays.asList("real", "real1", "real2"); // 배포에 사용될 profile (real은 기록을 위한 단일 배포용, real1, real2는 무중단 배포용)
        String defaultProfile = profiles.isEmpty() ? "default" : profiles.get(0);
        return profiles.stream()
                .filter(realProfiles::contains)
                .findAny()
                .orElse(defaultProfile);
    }

}
