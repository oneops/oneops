package com.oneops.cms.exceptions;

/**
 * The Class MDException.
 */
public class MDException  extends CmsBaseException {

    private static final long serialVersionUID = 1722078401937364163L;

    /**
     * Instantiates a new mD exception.
     *
     * @param errorCode the error code
     * @param mess the mess
     */
    public MDException(int errorCode, String mess) {
        super(errorCode, mess);
    }
}
