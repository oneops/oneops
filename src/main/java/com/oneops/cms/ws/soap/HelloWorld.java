package com.oneops.cms.ws.soap;

import javax.jws.WebService;

@WebService
public interface HelloWorld {
    String sayHi(String text);
}
