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
