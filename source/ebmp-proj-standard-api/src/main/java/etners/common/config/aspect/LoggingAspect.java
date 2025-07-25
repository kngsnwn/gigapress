package etners.common.config.aspect;

import common.util.string.StringUtil;
import etners.common.config.exception.ApplicationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @AfterThrowing(pointcut = "execution(* etners.standard.ddd.application..*(..))", throwing = "exception")
//  @AfterThrowing(pointcut = "execution(* etners.mcs..*Controller.*(..))", throwing = "exception")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
    if (skipLogging(exception)) {
      return;
    }
    logger.error("Method {} threw exception {}", joinPoint.getSignature().toShortString(), exception.getMessage());
    logger.error(StringUtil.extractStackTrace(exception));
  }

  private boolean skipLogging(Throwable exception) {
    return exception instanceof ApplicationException;
  }
}