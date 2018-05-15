package com.oneops.cms.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class to consolidate validation exception messages for UI
 */
public class ExceptionConsolidator<T extends CmsBaseException> {
    private final Class<T> clazz;
    private final int errorCode;
    private final int countOfErrorsToReport;
    private Set<String> errors;

    public ExceptionConsolidator(Class<T> clazz, int errorCode, int countOfErrorsToReport) {
        this.clazz = clazz;
        this.errorCode = errorCode;
        errors = new HashSet<>();
        this.countOfErrorsToReport = countOfErrorsToReport;
    }

    public T invokeChecked(Runnable r) {
        T result = null;
        try {
            r.run();
        } catch (Exception e) {
            if (clazz.isInstance(e)) {
                if (hasErrorCountExceeded()) {
                    rethrowExceptionIfNeeded();
                }
                errors.add(e.getMessage());
                result = (T) e;
            } else {
                throw e;
            }
        }
        return result;
    }

    public void rethrowExceptionIfNeeded() {
        rethrowExceptionIfNeeded(null, null, null);
    }

    public void rethrowExceptionIfNeeded(String prefix, String suffix, String separator) {
        if (!errors.isEmpty()) {
            try {
                throw clazz.getDeclaredConstructor(Integer.TYPE, String.class)
                        .newInstance(errorCode, (prefix == null ? "" : prefix) +
                                StringUtils.join(getErrorsToReport(), separator == null ? ";\n" : separator) +
                                (suffix == null ? "" : suffix));
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
