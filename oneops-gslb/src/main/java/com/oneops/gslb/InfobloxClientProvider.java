package com.oneops.gslb;

import com.oneops.infoblox.InfobloxClient;
import org.springframework.stereotype.Component;

@Component
public class InfobloxClientProvider {

  public InfobloxClient getInfobloxClient(String host, String user, String pwd) {
    return InfobloxClient.builder().
        endPoint(host).
        userName(user).
        password(pwd).
        tlsVerify(false).build();
  }

}
