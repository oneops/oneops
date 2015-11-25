package com.oneops.cms.ws.soap;

import javax.jws.WebService;

@WebService(endpointInterface = "com.oneops.cms.ws.soap.HelloWorld")
public class HelloWorldImpl implements HelloWorld {
 
	public String sayHi(String text) {
		 System.out.println("sayHi called");
	        return "Hello " + text;
	}

}
