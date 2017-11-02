package com.oneops.cms.ds;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class DataAccessAspect {

  private static final String HEADER_DATA_CONSISTENCY = "X-Cms-Data-Consistency";

  private Logger logger = Logger.getLogger(this.getClass());

  @Value("${IS_QUERY_STANDBY_ENABLED:false}")
  private boolean isQueryStandByEnabled;


  @Around("@annotation(ReadOnlyDataAccess)")
  public Object accessDataReadOnly(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String dataConsistency = request.getHeader(HEADER_DATA_CONSISTENCY);
    Object returnValue;
    if (isQueryStandByEnabled && "weak".equals(dataConsistency)) {
      if (logger.isDebugEnabled()) {
        logger.debug("marking " + joinPoint.getSignature().getName() + " read only ");
      }
      DataTypeHolder.setReadOnlyData();
      try {
        returnValue = joinPoint.proceed();
      } catch (Throwable throwable) {
        DataTypeHolder.clear();
        logger.info("retrying the request " + joinPoint.getSignature().getName() + " in primary");
        returnValue = joinPoint.proceed();
      } finally {
        DataTypeHolder.clear();
      }
      return returnValue;
    }
    return joinPoint.proceed();
  }

  public void setQueryStandByEnabled(boolean queryStandByEnabled) {
    isQueryStandByEnabled = queryStandByEnabled;
  }
}
