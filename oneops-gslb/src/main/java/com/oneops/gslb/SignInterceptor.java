package com.oneops.gslb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;

public class SignInterceptor implements Interceptor {

  private String user;
  private String authKey;

  public final static String AUTH_HEADER_NAME = "X-Torbit-Auth";

  public SignInterceptor(String user, String authKey) {
    this.user = user;
    this.authKey = authKey;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request original = chain.request();
    HttpUrl originalHttpUrl = original.url();
    String query = originalHttpUrl.encodedQuery();

    StringBuilder builder = new StringBuilder(originalHttpUrl.encodedPath());
    if (StringUtils.isNotEmpty(query)) {
      builder.append("?");
      builder.append(query);
      builder.append("&");
    }
    else {
      builder.append("?");
    }
    String timeVal = Long.toString(System.currentTimeMillis() / 1000l);
    builder.append("time=").append(timeVal);
    String outerHash = sha1hex(this.authKey + sha1hex(this.authKey + builder.toString()));

    HttpUrl newUrl = originalHttpUrl.newBuilder().
        addQueryParameter("time", timeVal).
        addQueryParameter("sig", outerHash).build();
    Request.Builder requestBuilder = original.newBuilder().
        url(newUrl).
        addHeader(AUTH_HEADER_NAME, user);
    Request request = requestBuilder.build();

    return chain.proceed(request);
  }


  private static String sha1hex(String str) throws UnsupportedEncodingException {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      System.err.println("Caught NoSuchAlgorithmException: " + e.getMessage());
    }

    md.update(str.getBytes("UTF-8"));
    return bytesToHex(md.digest());
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexStr = new StringBuilder();
    for (byte byt : bytes) {
      hexStr.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
    }
    return hexStr.toString();
  }


}
