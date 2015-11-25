package com.oneops.cms.ws.rest.util;

import java.io.Serializable;

import com.oneops.cms.util.CmsError;

public class ErrorResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private int code;
    private int errorCode;
    private String message;

	public ErrorResponse( int code, CmsError e ) {
		this.code = code;
		//Deprecated
        if(e instanceof Exception ) {
            this.message = ((Exception)e).getMessage();
        }
        this.errorCode = e.getErrorCode();
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

    public int getErrorCode() {
        return errorCode;
    }
}
