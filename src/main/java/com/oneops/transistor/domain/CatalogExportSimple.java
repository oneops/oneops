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

import java.util.List;

import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;

public class CatalogExportSimple extends CatalogExportBasic {
	private List<CmsCISimple> cis;
	private List<CmsCIRelationSimple> relations;

	public List<CmsCISimple> getCis() {
		return cis;
	}
	public void setCis(List<CmsCISimple> cis) {
		this.cis = cis;
	}
	public List<CmsCIRelationSimple> getRelations() {
		return relations;
	}
	public void setRelations(List<CmsCIRelationSimple> relations) {
		this.relations = relations;
	}
}
