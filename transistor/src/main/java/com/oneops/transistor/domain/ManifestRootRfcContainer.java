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

import java.util.ArrayList;
import java.util.List;

import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;

public class ManifestRootRfcContainer {
	private List<Long> templateCis = new ArrayList<>();
	private CmsRfcCI rfcCI;
	private List<CmsRfcRelation> toRfcRelation = new ArrayList<>();
	private List<CmsRfcRelation> fromRfcRelation = new ArrayList<>();


	public List<Long> getTemplateCis() {
		return templateCis;
	}

	public void setTemplateCis(List<Long> templateCis) {
		this.templateCis = templateCis;
	}

	public CmsRfcCI getRfcCI() {
		return rfcCI;
	}

	public void setRfcCI(CmsRfcCI rfcCI) {
		this.rfcCI = rfcCI;
	}

	public List<CmsRfcRelation> getToRfcRelation() {
		return toRfcRelation;
	}

	public void setToRfcRelation(List<CmsRfcRelation> toRfcRelation) {
		this.toRfcRelation = toRfcRelation;
	}

	public List<CmsRfcRelation> getFromRfcRelation() {
		return fromRfcRelation;
	}

	public void setFromRfcRelation(List<CmsRfcRelation> fromRfcRelation) {
		this.fromRfcRelation = fromRfcRelation;
	}

}
