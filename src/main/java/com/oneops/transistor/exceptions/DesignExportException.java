package com.oneops.transistor.exceptions;

import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;

public class DesignExportException extends CmsBaseException implements CmsError {

	private static final long serialVersionUID = -1680106525715487629L;

	public DesignExportException(int errorCode, String errMsg){
		super(errorCode, errMsg);
	}

}
