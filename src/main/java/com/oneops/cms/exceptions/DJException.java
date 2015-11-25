package com.oneops.cms.exceptions;

/**
 * The Class DJException.
 */
public class DJException extends CmsBaseException {

	private static final long serialVersionUID = -5658441061630579242L;

	/**
	 * Instantiates a new dJ exception.
	 *
	 * @param errorCode the error code
	 * @param errMsg the err msg
	 */
	public DJException(int errorCode, String errMsg) {
		super(errorCode, errMsg);
	}
	
}
