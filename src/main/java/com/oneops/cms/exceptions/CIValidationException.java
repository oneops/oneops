package com.oneops.cms.exceptions;

/**
 * The Class CIValidationException.
 */
public class CIValidationException extends CmsBaseException {

	private static final long serialVersionUID = -8243238521173364982L;

	/**
	 * Instantiates a new cI validation exception.
	 *
	 * @param errorCode the error code
	 * @param errMsg the err msg
	 */
	public CIValidationException(int errorCode, String errMsg) {
		super(errorCode, errMsg);
	}
	
}
