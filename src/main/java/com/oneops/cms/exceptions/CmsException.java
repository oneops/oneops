package com.oneops.cms.exceptions;

/**
 * The Class CmsException.
 */
public class CmsException extends CmsBaseException {

    private static final long serialVersionUID = 5476850671630630581L;

	/**
	 * Instantiates a new cms exception.
	 *
	 * @param errorCode the error code
	 * @param msg the msg
	 */
	public CmsException(int errorCode, String msg) {
		super(errorCode, msg);
	}
}
