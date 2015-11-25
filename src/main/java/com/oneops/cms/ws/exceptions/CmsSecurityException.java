package com.oneops.cms.ws.exceptions;

import com.oneops.cms.util.CmsError;

public class CmsSecurityException extends SecurityException implements CmsError {
	
	private static final long serialVersionUID = 1L;
    private int errorCode;

	public CmsSecurityException() {
		super();
	}
	
	public CmsSecurityException(String errMsg) {
		super(errMsg);
	}

    @Override
    public int getErrorCode() {
        return errorCode;
    }
}
