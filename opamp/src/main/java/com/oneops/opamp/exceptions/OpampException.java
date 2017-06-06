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
package com.oneops.opamp.exceptions;

/**
 * custom domain Exception inherting from Exception
 *
 */
public class OpampException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new opamp exception.
	 */
	public OpampException() {
		super();
	}

	/**
	 * Instantiates a new opamp exception.
	 *
	 * @param e the e
	 */
	public OpampException(Exception e) {
		super(e);
	}
	
	/**
	 * Instantiates a new opamp exception.
	 *
	 * @param errMsg the err msg
	 */
	public OpampException(String errMsg) {
		super(errMsg);
	}
}
