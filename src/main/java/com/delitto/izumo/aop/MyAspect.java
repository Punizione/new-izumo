package com.delitto.izumo.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Log4j2
@Aspect
@Component

public class MyAspect {
//    @Pointcut("execution(public * net.lz1998.pbbot.bot.ApiSender.*(..))")
//    private void pointcut() {
//    }

//    @Around("pointcut()")
//    private Object logHandler(ProceedingJoinPoint pjp) throws Throwable {
//        try {
//            //log.info(pjp.getSignature() + " 被调用");
//            return pjp.proceed();
//        } catch (Throwable e) {
//            log.error("Handling error");
//            throw e;
//        }
//    }
}
