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
package com.oneops.amq.plugins;

/**
 * The Class CmsAuthException.
 */
public class CmsAuthException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new cms auth exception.
	 */
	public CmsAuthException() {
	}
	
	/**
	 * Instantiates a new cms auth exception.
	 *
	 * @param error the error
	 */
	public CmsAuthException(String error) {
		super(error);
	}
	
}
