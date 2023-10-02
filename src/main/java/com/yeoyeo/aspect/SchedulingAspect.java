package com.yeoyeo.aspect;

import com.yeoyeo.application.common.method.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Aspect
@Component
public class SchedulingAspect {

    @Autowired
    private CommonMethod commonMethod;

    @Pointcut("execution(* com.yeoyeo.application..*Scheduler.*(..))")
    private void schedulerPointcut() {}
    @Pointcut("execution(* com.yeoyeo.application..*Generator.*(..))")
    private void generatorPointcut() {}

    @Transactional
    @Around("schedulerPointcut() || generatorPointcut()")
    public void schedulerLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        String scheduleName = joinPoint.getSignature().getName();
        try {
            log.info("=== Server Profile : {} / Scheduler : {} ===", commonMethod.getServerProfile(), scheduleName);
            commonMethod.startScheduling(scheduleName);
            joinPoint.proceed();
            commonMethod.endScheduling(scheduleName);
            log.info("-- Scheduler : {} [SUCCESS] --", scheduleName);
        } catch (Exception exception) {
            log.error("-- Scheduler : {} [ERROR] (1) --", scheduleName);
            log.error("-- Scheduler : {} [ERROR] (2) --", exception.getMessage());
            exception.printStackTrace();
        } finally {
            log.info("=== Scheduler : {} [END] ===", scheduleName);
        }
    }

}
