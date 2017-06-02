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
package com.oneops.cms.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * The Interface CmsCrypto.
 */
public interface CmsCrypto {
	String encrypt(String instr) throws GeneralSecurityException, IOException;
	String decrypt(String instr) throws GeneralSecurityException;
	String decryptVars(String instr) throws GeneralSecurityException;
	String ENC_PREFIX = "::ENCRYPTED::";
	String ENC_DUMMY = "--ENCRYPTED--";
	String ENC_VAR_PREFIX = "::VAR-ENCRYPTED::";
	String ENC_VAR_SUFFIX = "::VAR-END::";

	default boolean isEncrypted(String instr){
		return check(instr, ENC_PREFIX);
	}
	default boolean isVarEncrypted(String instr){
		return check(instr, ENC_VAR_PREFIX);
	}

	default boolean check(String instr, String checkStr) {
		if (instr==null) return  false;
		return instr.startsWith(checkStr);
	}


}
