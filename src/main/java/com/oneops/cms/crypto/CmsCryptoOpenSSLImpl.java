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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Hex;

/**
 * The Class CmsCryptoOpenSSLImpl.
 */
public class CmsCryptoOpenSSLImpl implements CmsCrypto {

	private String privateKeyPemFile;
	private String publicKeyPemFile;
	private String passPhrase;
	private Cipher encryptor = null;
	private Cipher decryptor = null;
	static Logger logger = Logger.getLogger(CmsCryptoOpenSSLImpl.class);

	
	//private static final String ENC_PREFIX = "::ENCRYPTED::";
	
	/**
	 * Sets the private key pem file.
	 *
	 * @param privateKeyPemFile the new private key pem file
	 */
	public void setPrivateKeyPemFile(String privateKeyPemFile) {
		this.privateKeyPemFile = privateKeyPemFile;
	}

	/**
	 * Sets the public key pem file.
	 *
	 * @param publicKeyPemFile the new public key pem file
	 */
	public void setPublicKeyPemFile(String publicKeyPemFile) {
		this.publicKeyPemFile = publicKeyPemFile;
	}

	/**
	 * Sets the pass phrase.
	 *
	 * @param passPhrase the new pass phrase
	 */
	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	/**
	 * Encrypt.
	 *
	 * @param instr the instr
	 * @return the string
	 * @throws GeneralSecurityException the general security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public String encrypt(String instr) throws GeneralSecurityException, IOException {
        byte[]  in = instr.getBytes();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
	        byte[]  out = encryptor.doFinal(in);
			Hex.encode(out, os);
		} catch (IOException e) {
			e.printStackTrace();
			initEncryptorDecryptor();
			throw new GeneralSecurityException(e);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			initEncryptorDecryptor();
			throw new GeneralSecurityException("The value is too long for encryption");
		}
        return ENC_PREFIX + os.toString();
	}

	/**
	 * Decrypt.
	 *
	 * @param instr the instr
	 * @return the string
	 * @throws GeneralSecurityException the general security exception
	 */
	@Override
	public String decrypt(String instr)  throws GeneralSecurityException {
		byte[]  in;
		if (instr.startsWith(CmsCrypto.ENC_PREFIX)) {
			in =  Hex.decode(instr.substring(CmsCrypto.ENC_PREFIX.length()));
		} else {
			in = Hex.decode(instr);
		}
        byte[]  out = decryptor.doFinal(in);
        return new String(out);
	}

	/**
	 * Inits the.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws GeneralSecurityException the general security exception
	 */
	public void init() throws IOException, GeneralSecurityException {
		Security.addProvider(new BouncyCastleProvider());
		this.publicKeyPemFile = System.getenv("CMS_PUB_PEM");
		if (this.publicKeyPemFile == null) {
			this.publicKeyPemFile = System.getProperty("com.kloopz.crypto.cms_pub_pem");
		}
		this.privateKeyPemFile = System.getenv("CMS_PRIV_PEM");
		if (this.privateKeyPemFile == null) {
			this.privateKeyPemFile = System.getProperty("com.kloopz.crypto.cms_priv_pem");
		}

		this.passPhrase = System.getenv("CMS_PEM_PASS");
		if (this.passPhrase == null) {
			this.passPhrase = System.getProperty("com.kloopz.crypto.cms_pem_pass");
		}
		 
		//Map<String,String> envVars = System.getenv();
		/*if (this.passPhrase == null) {
			 Console cons;
			 char[] passwd;
			 if ((cons = System.console()) != null &&
			     (passwd = cons.readPassword("[%s]", "Password:")) != null) {
				 this.passPhrase = new String(passwd);
			 }
		}*/
		
		initEncryptorDecryptor();
		
	}
	
	/**
	 * Inits the.
	 *
	 * @param pubKeyFile the pub key file
	 * @param privKeyFile the priv key file
	 * @param passPhrase the pass phrase
	 * @throws GeneralSecurityException the general security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void init(String pubKeyFile, String privKeyFile, String passPhrase) throws GeneralSecurityException, IOException {
		this.publicKeyPemFile = pubKeyFile;
		this.privateKeyPemFile = privKeyFile;
		this.passPhrase = passPhrase;
		initEncryptorDecryptor();
	}
	
	private void initEncryptorDecryptor() throws GeneralSecurityException, IOException {
		if (this.publicKeyPemFile != null) {
			PublicKey pubKey = getPubKeyFromPem();
			encryptor = Cipher.getInstance("RSA", "BC");
			encryptor.init(Cipher.ENCRYPT_MODE, pubKey);
			logger.info("Successfully initialized openssl encriptor");
		}
		
		if (this.privateKeyPemFile != null && this.passPhrase != null) {
			PrivateKey privKey = getPrivKeyFromPem();
	        decryptor = Cipher.getInstance("RSA", "BC");
	        decryptor.init(Cipher.DECRYPT_MODE, privKey);
			logger.info("Successfully initialized openssl decriptor");
		}
	}
	
	
	private PublicKey getPubKeyFromPem() throws IOException, GeneralSecurityException {
		 
		PublicKey pk = null;
		PEMReader       pemRd = openPEMResource(publicKeyPemFile, null);
		Object          o;
		
		while ((o = pemRd.readObject()) != null) {
		    if (o instanceof PublicKey) {
		        pk = (PublicKey)o;
		    }
		}
		return pk;
	}

	private PrivateKey getPrivKeyFromPem() throws IOException, GeneralSecurityException {
		 
		PrivateKey pk = null;
		PasswordFinder  pGet = new Password(passPhrase.toCharArray());
		PEMReader       pemRd = openPEMResource(privateKeyPemFile, pGet);
		Object          o;
		KeyPair         pair;
		while ((o = pemRd.readObject()) != null) {
		    if (o instanceof KeyPair) {
		    	pair = (KeyPair)o;
		    	pk = pair.getPrivate();
		    }
		}
		return pk;
	}

	 
	 private PEMReader openPEMResource(
		        String          fileName,
		        PasswordFinder  pGet) throws FileNotFoundException
	 {
		 InputStream res = new BufferedInputStream(new FileInputStream(fileName));
	     Reader fRd = new BufferedReader(new InputStreamReader(res));
	     return new PEMReader(fRd, pGet);
	 } 
	 
	private class Password implements PasswordFinder	   {
		char[]  password;
	
		Password(char[] word) {
			this.password = word;
	    }
	
	    public char[] getPassword() {
	           return password;
	    }
	}

	@Override
	public String decryptVars(String instr) throws GeneralSecurityException {
		// TODO Auto-generated method stub
		return null;
	}
}
