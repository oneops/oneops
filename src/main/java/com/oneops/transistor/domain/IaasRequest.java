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
package com.oneops.transistor.domain;

import java.io.Serializable;
import java.util.List;

public class IaasRequest   implements Serializable{

	private static final long serialVersionUID = 1L;

	private List<IaasBindingTriplet> iaasList;
	
	public void setIaasList(List<IaasBindingTriplet> iaasList) {
		this.iaasList = iaasList;
	}
	public List<IaasBindingTriplet> getIaasList() {
		return iaasList;
	}
}
