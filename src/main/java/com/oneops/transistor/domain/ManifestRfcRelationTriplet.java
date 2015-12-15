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

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

public class ManifestRfcRelationTriplet {

	private CmsRfcCI toRfcCI;
	private CmsRfcCI fromRfcCI;
	private CmsRfcRelation rfcRelation;
	
	public CmsRfcCI getToRfcCI() {
		return toRfcCI;
	}
	public void setToRfcCI(CmsRfcCI toRfcCI) {
		this.toRfcCI = toRfcCI;
	}
	public CmsRfcCI getFromRfcCI() {
		return fromRfcCI;
	}
	public void setFromRfcCI(CmsRfcCI fromRfcCI) {
		this.fromRfcCI = fromRfcCI;
	}
	public CmsRfcRelation getRfcRelation() {
		return rfcRelation;
	}
	public void setRfcRelation(CmsRfcRelation rfcRelation) {
		this.rfcRelation = rfcRelation;
	}
	
	
}
