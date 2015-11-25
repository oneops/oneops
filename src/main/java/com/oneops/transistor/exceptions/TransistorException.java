package com.oneops.transistor.exceptions;

import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;

public class TransistorException  extends CmsBaseException implements CmsError {

	private static final long serialVersionUID = -7949939801377744877L;

	public TransistorException(int errorCode, String errMsg){
		super(errorCode, errMsg);
	}
}
