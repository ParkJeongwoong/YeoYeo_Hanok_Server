package com.yeoyeo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Pointcut
    @Pointcut("execution(* com.yeoyeo.adapter.controller..*.*(..))")
    private void controllerPointcut() {}
    @Pointcut("execution(* com.yeoyeo.application..*Service.*(..))")
    private void servicePointcut() {}

    // Aspect
    @Before("controllerPointcut()")
    public void controllerLogging(JoinPoint joinPoint) {
        log.info("Controller : {} / Method : {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }
    @Before("servicePointcut()")
    public void serviceLogging(JoinPoint joinPoint) {
        log.info("Service : {} / Method : {}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
    }

}
