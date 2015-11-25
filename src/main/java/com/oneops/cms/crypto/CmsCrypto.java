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
	static final String ENC_PREFIX = "::ENCRYPTED::";
	static final String ENC_DUMMY = "--ENCRYPTED--";
	static final String ENC_VAR_PREFIX = "::VAR-ENCRYPTED::";
	static final String ENC_VAR_SUFFIX = "::VAR-END::";

}
