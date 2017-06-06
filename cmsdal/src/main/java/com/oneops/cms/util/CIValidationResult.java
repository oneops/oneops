/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.cms.util;

/**
 * The Class CIValidationResult.
 */
public class CIValidationResult {
	private boolean isValidated;
	private String errorMsg;
	private boolean needNScreation;
	private boolean useClassNameInNS;
	
	/**
	 * Checks if is validated.
	 *
	 * @return true, if is validated
	 */
	public boolean isValidated() {
		return isValidated;
	}
	
	/**
	 * Sets the validated.
	 *
	 * @param isValidated the new validated
	 */
	public void setValidated(boolean isValidated) {
		this.isValidated = isValidated;
	}
	
	/**
	 * Gets the error msg.
	 *
	 * @return the error msg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}
	
	/**
	 * Sets the error msg.
	 *
	 * @param errorMsg the new error msg
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg + "\n";
	}
	
	/**
	 * Adds the error msg.
	 *
	 * @param errorMsg the error msg
	 */
	public void addErrorMsg(String errorMsg) {
		if (this.errorMsg == null) {
			this.errorMsg = errorMsg + "\n";
		} else {
			this.errorMsg += errorMsg + "\n";
		}
	}

	/**
	 * Checks if is need n screation.
	 *
	 * @return true, if is need n screation
	 */
	public boolean isNeedNScreation() {
		return needNScreation;
	}
	
	/**
	 * Sets the need n screation.
	 *
	 * @param needNScreation the new need n screation
	 */
	public void setNeedNScreation(boolean needNScreation) {
		this.needNScreation = needNScreation;
	}
	
	/**
	 * Indicates that during NS path creation ClassName needs to be used 
	 *
	 * @param needNScreation the new need n screation
	 */
	public boolean getUseClassNameInNS() {
		return useClassNameInNS;
	}

	public void setUseClassNameInNS(boolean useClassNameInNS) {
		this.useClassNameInNS = useClassNameInNS;
	}
	
}
