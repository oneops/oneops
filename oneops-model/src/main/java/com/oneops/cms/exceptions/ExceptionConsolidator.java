package com.oneops.cms.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class to consolidate validation exception messages for UI
 */
public class ExceptionConsolidator {

  private final Class<? extends CmsBaseException> exception;
  private final int errorCode;
  private final int countOfErrorsToReport;
  private Set<String> errors;


  public ExceptionConsolidator(Class<? extends CmsBaseException> exception, int errorCode,
    int countOfErrorsToReport) {
    this.errorCode = errorCode;
    this.exception = exception;
    errors = new HashSet<>();
    this.countOfErrorsToReport = countOfErrorsToReport;
  }


  public void invokeChecked(Runnable r) {
    try {
      r.run();
    } catch (Exception e) {
      if (exception.isInstance(e)) {
        if (hasErrorCountExceeded()) {
          rethrowExceptionIfNeeded();
        }
        errors.add(e.getMessage());

      } else {
        throw e;
      }
    }
  }


  public void rethrowExceptionIfNeeded() {
    if (!errors.isEmpty()) {
      try {
        throw exception.getDeclaredConstructor(Integer.TYPE, String.class)
          .newInstance(errorCode, StringUtils.join(
            getErrorsToReport(), ";\n"));
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(errors.toString());
      }
    }
  }

  private Set<String> getErrorsToReport() {
    return hasErrorCountExceeded() ? errors.stream().limit(countOfErrorsToReport).collect(
      Collectors.toSet()) : errors;
  }

  private boolean hasErrorCountExceeded() {
    return errors.size() >= countOfErrorsToReport;
  }
}
