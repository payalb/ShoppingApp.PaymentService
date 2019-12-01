package com.java.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class LoggingService {
	
	Logger logger = LoggerFactory.getLogger(LoggingService.class);
	
	@Around("execution(* com.java.dao.*.*(..))")
	public Object log(ProceedingJoinPoint jp) throws Throwable {
		logger.info("Inside method"+ jp.getSignature().getName());
		Object result=jp.proceed();
		logger.info("Exiting method"+ jp.getSignature().getName());
		return result;
	}
	
}
