package com.oneops.cms.exceptions;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class OpsException.
 */
public class OpsException  extends CmsBaseException {

    private static final long serialVersionUID = 9168282442897406033L;
    private Map<String,String> exceptionDetails = new HashMap<String,String>(); 

    public enum ExceptionDetailKey{
    	BLOCKING_PROCEDURE_CI_ID,
    	PROCEDURE_ID,
    	PROCEDURE_NAME,
    	PROCEDURE_STATE, 
    	MAX_EXEC_ORDER, 
    	ARG_LIST
    }
    /**
     * Instantiates a new ops exception.
     *
     * @param errorCode the error code
     * @param message the message
     */
    public OpsException(int errorCode, String message) {
        super(errorCode, message);
        this.exceptionDetails=new HashMap<String,String>();
    }

    /**
     * Instantiates new ops exception
     * @param errorCode
     * @param message
     * @param exceptionDetails map
     */
	public OpsException(int errorCode, String message, Map<String, String> exceptionDetails) {
        super(errorCode, message);
		this.exceptionDetails = exceptionDetails;
	}
    
   
	/**
	 * If the exception is due to a procedure blocking, this method returns the 
	 * id of the blocking procedure - if no blocker, it returns null
	 * @return id or null
	 */
	public Long getBlockingProcedureId(){
		if (this.exceptionDetails.containsKey(ExceptionDetailKey.BLOCKING_PROCEDURE_CI_ID.name())){
			return Long.valueOf(this.exceptionDetails.get(ExceptionDetailKey.BLOCKING_PROCEDURE_CI_ID.name()));
		} else {
			return null;
		}
	}

	/**
	 * @return the exceptionDetails Map
	 */
	public Map<String, String> getExceptionDetails() {
		return exceptionDetails;
	}

	/**
	 * Adds an entry into the exceptionDetails Map. Useful for clients who catch
	 * this and want to see specifics about the error
	 * @param key Entry key in the map
	 * @param value Entry value in the map
	 * @return this OpsException
	 */
	public OpsException set(String key, String value) {
		this.exceptionDetails.put(key, value);
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OpsException [exceptionDetails=");
		builder.append(exceptionDetails);
		builder.append(", error=");
		builder.append(error);
		builder.append(", params=");
		builder.append(params);
		builder.append("]");
		return builder.toString();
	}


	
}
