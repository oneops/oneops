package com.oneops.transistor.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
