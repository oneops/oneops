package com.oneops.cms.exceptions;

import com.oneops.cms.util.CmsError;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to consolidate validation exception messages for UI
 */
public class ExceptionConsolidator {
    private final Class<? extends CmsBaseException> exception;
    private final int errorCode;
    private List<String> errors; 
    
    
    public ExceptionConsolidator(Class<? extends CmsBaseException> exception, int errorCode) {
        this.errorCode = errorCode;
        this.exception = exception;
        errors = new ArrayList<>();
    }

    public void invokeChecked(Runnable r) {
        try {
            r.run();
        } catch (Exception e){
            if (exception.isInstance(e)) {
                errors.add(e.getMessage());
            } else {
                throw e;
            }
        }
    }
    
    
    public void rethrowExceptionIfNeeded(){
        if (!errors.isEmpty()){
            try {
                throw exception.getDeclaredConstructor(Integer.TYPE, String.class).newInstance(errorCode, StringUtils.join(errors, ",\n"));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(errors.toString());
            }
        }
    }
}
