package com.oneops.cms.exceptions;

import com.oneops.cms.util.CmsError;

/**
 * Base exception that contains CmsError.
 */
public class CmsBaseException  extends RuntimeException implements CmsError {

    private static final long serialVersionUID = -223420208060578824L;
    protected int error;
    protected Object params;

    /**
     * Instantiates a new cms base exception.
     */
    public CmsBaseException() {
    }

    /**
     * Instantiates a new cms base exception.
     *
     * @param message the message
     */
    public CmsBaseException(String message) {
        super(message);
    }

    /**
     * Instantiates a new cms base exception.
     *
     * @param errorCode the error code
     * @param mess the mess
     */
    public CmsBaseException(int errorCode, String mess) {
        super(mess);
        this.error = errorCode;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return error;
    }
}
