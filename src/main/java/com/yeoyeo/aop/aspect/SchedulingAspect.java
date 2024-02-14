package com.yeoyeo.aop.aspect;

import com.yeoyeo.application.common.method.CommonMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class SchedulingAspect {

    private final CommonMethod commonMethod;

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
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        if (methodSignature.getMethod().getAnnotation(com.yeoyeo.aop.annotation.SingleJob.class) != null
            && !methodSignature.getMethod().getAnnotation(com.yeoyeo.aop.annotation.SingleJob.class).scheduleName().isEmpty()) {
            scheduleName = methodSignature.getMethod().getAnnotation(com.yeoyeo.aop.annotation.SingleJob.class).scheduleName();
        }
        else {
            scheduleName = methodSignature.getName();
        }
        try {
            log.info("=== Server Profile : {} / Scheduler : {} ===", commonMethod.getServerProfile(), scheduleName);
            commonMethod.startScheduling(scheduleName);
            joinPoint.proceed();
            commonMethod.endScheduling(scheduleName);
            log.info("-- Scheduler : {} [SUCCESS] --", scheduleName);
        } catch (Exception exception) {
            log.error("-- Scheduler : {} [ERROR] (1) --", scheduleName);
            log.error("-- Scheduler : {} [ERROR] (2) --", exception.getMessage());
        } finally {
            log.info("=== Scheduler : {} [END] ===", scheduleName);
        }
    }

}
