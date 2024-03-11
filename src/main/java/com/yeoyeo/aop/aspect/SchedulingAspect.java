package com.yeoyeo.aop.aspect;

import com.yeoyeo.application.common.exception.SchedulingException;
import com.yeoyeo.application.common.method.CommonMethod;
import com.yeoyeo.application.common.service.WebClientService;
import com.yeoyeo.application.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.simple.JSONObject;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class SchedulingAspect {

    WebClientService webClientService;
    private final CommonMethod commonMethod;
    private final MessageService messageService;
    private final RedissonClient redissonClient;

    @Value("schedule.key")
    String SCHEDULE_KEY;

    @Pointcut("execution(* com.yeoyeo.application..*Scheduler.*(..))")
    private void schedulerPointcut() {}
    @Pointcut("execution(* com.yeoyeo.application..*Generator.*(..))")
    private void generatorPointcut() {}
    @Pointcut("@annotation(com.yeoyeo.aop.annotation.SingleJob)")
    private void singleJobPointcut() {}

    @Transactional
    @Around("schedulerPointcut() || generatorPointcut() || singleJobPointcut()")
    public void schedulerLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        String scheduleName;
        boolean isSingleJob = false;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        if (methodSignature.getMethod().getAnnotation(com.yeoyeo.aop.annotation.SingleJob.class) != null
            && !methodSignature.getMethod().getAnnotation(com.yeoyeo.aop.annotation.SingleJob.class).scheduleName().isEmpty()) {
            scheduleName = methodSignature.getMethod().getAnnotation(com.yeoyeo.aop.annotation.SingleJob.class).scheduleName();
            isSingleJob = true;
        }
        else {
            scheduleName = methodSignature.getName();
        }

        String serverProfile = commonMethod.getServerProfile();
        try {
            log.info("=== Server Profile : {} / Scheduler : {} ===", serverProfile, scheduleName);
            commonMethod.startScheduling(scheduleName);
            joinPoint.proceed();
            commonMethod.endScheduling(scheduleName);
            log.info("-- Scheduler : {} [SUCCESS] --", scheduleName);
        } catch (RedisConnectionFailureException exception) {
            log.error("-- Scheduler : Redis Connection Failure --");
            log.info("[Fallback Start] - profile : {}", serverProfile);
            scheduleFallback(joinPoint, serverProfile, isSingleJob, scheduleName);
        } catch (SchedulingException exception) {
            log.error("-- Scheduler : {} [ERROR] (1) --", scheduleName);
            log.error("-- Scheduler : {} [ERROR] (2) --", exception.getMessage());
        } finally {
            log.info("=== Scheduler : {} [END] ===", scheduleName);
        }
    }

    private void scheduleFallback(ProceedingJoinPoint joinPoint, String profile, boolean isSingleJob, String scheduleName) {
        try {
            if (profile.equals("real1")) {
                joinPoint.proceed();
            }
            else if (profile.equals("real2")) {
                String url_profile1 = "http://localhost:8091/actuator/health"; // profile1 주소
                JSONObject response = webClientService.get("application/json;charset=UTF-8", url_profile1);
                if (response.get("status").equals("UP")) {
                    log.info("Redis Connection Failure로 스케쥴 작업 profile1에 위임");
                    // schedule name 전송 -> 해당 서버에서 판단
                    String url_singleJob_profile1 = "http://localhost:8091/schedule/run/" + scheduleName;
                    String token = commonMethod.encodeToken(SCHEDULE_KEY, scheduleName);
                    webClientService.post("application/json;charset=UTF-8", url_singleJob_profile1, null, "schedule-token", token);
                }
                else {
                    joinPoint.proceed();
                }
            }
        } catch (Throwable throwable) {
            log.error("Scheduler Fallback Error : {}", throwable.getMessage());
            messageService.sendDevMsg("Scheduler Fallback Error : " + throwable.getMessage());
        }
    }

}
