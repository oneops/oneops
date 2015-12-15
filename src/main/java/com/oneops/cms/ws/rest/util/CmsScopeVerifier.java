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
package com.oneops.cms.ws.rest.util;

import com.oneops.cms.cm.domain.CmsCIBasic;
import com.oneops.cms.cm.domain.CmsCIRelationBasic;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCIBasic;
import com.oneops.cms.dj.domain.CmsRfcRelationBasic;
import com.oneops.cms.ws.exceptions.CmsSecurityException;

public class CmsScopeVerifier {
	
	private static final String pubNsPrefix = "/public";
	
	
	public void verifyScope(String scope, CmsRfcCIBasic rfc) {
		if (scope != null && !rfc.getNsPath().startsWith(scope) && !rfc.getNsPath().startsWith(pubNsPrefix)) {
			throw new CmsSecurityException("bad scope");
		}	
	}

	public void verifyScope(String scope, CmsCIBasic ci) {
		if (scope != null && !ci.getNsPath().startsWith(scope) && !ci.getNsPath().startsWith(pubNsPrefix)) {
			throw new CmsSecurityException("bad scope");
		}	
	}

	public void verifyScope(String scope, String ns) {
		if (scope != null && !ns.startsWith(scope) && !ns.startsWith(pubNsPrefix)) {
			throw new CmsSecurityException("bad scope");
		}	
	}
	
	public void verifyScope(String scope, CmsCIRelationBasic rel) {
		if (scope != null && !rel.getNsPath().startsWith(scope) && !rel.getNsPath().startsWith(pubNsPrefix)) {
			throw new CmsSecurityException("bad scope");
		}	
	}

	public void verifyScope(String scope, CmsRfcRelationBasic rfc) {
		if (scope != null && !rfc.getNsPath().startsWith(scope) && !rfc.getNsPath().startsWith(pubNsPrefix)) {
			throw new CmsSecurityException("bad scope");
		}	
	}

	public void verifyScope(String scope, CmsRelease release) {
		if (scope != null && !release.getNsPath().startsWith(scope)) {
			throw new CmsSecurityException("bad scope");
		}	
	}

	public void verifyScope(String scope, CmsDeployment dpmt) {
		if (scope != null && !dpmt.getNsPath().startsWith(scope)) {
			throw new CmsSecurityException("bad scope");
		}	
	}
	
}
