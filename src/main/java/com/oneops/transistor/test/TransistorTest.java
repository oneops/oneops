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
package com.oneops.transistor.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.oneops.cms.cm.ops.service.OpsProcedureProcessor;

public class TransistorTest  {

	
	//private ApplicationContext ctx;
	

	public static void main(String[] args) throws IOException {
		/*Gson gson = new Gson();
		ProcedureRequest pr = new ProcedureRequest(); 
		pr.setCiId(123);
		pr.setProcedureCiId(123);
		Map<String,String> argList = new HashMap<String, String>();
		argList.put("arg1", "wtf");
		argList.put("arg2", "wtfagain");
		pr.setArgList(argList);
		System.out.println(gson.toJson(pr));
		*/
		throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, null);

	}

	private static String getProcDef(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
	}
	
	
}
