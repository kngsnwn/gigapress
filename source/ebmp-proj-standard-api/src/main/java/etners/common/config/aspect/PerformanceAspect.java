package etners.common.config.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

/*  @Around("execution(* etners.standard..*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    Object proceed = joinPoint.proceed();
    long executionTime = System.currentTimeMillis() - start;

    logger.info("Method {} executed in {} ms", joinPoint.getSignature().toShortString(), executionTime);

    return proceed;
  }*/
}
