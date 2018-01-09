package com.oneops.gslb;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TorbitClient {

  protected static final MediaType JSON = MediaType.parse("application/json");

  private TorbitApi torbit;

  private Retrofit retrofit;

  public static TorbitClient getClient(TorbitConfig config) throws Exception {
    TorbitClient client = new TorbitClient();
    client.newTorbitApi(config);
    return client;
  }

  private void newTorbitApi(TorbitConfig config) throws Exception {
    Gson gson = new Gson();
    X509TrustManager trustManager = getTrustManager();
    TrustManager[] trustAllCerts = new TrustManager[]{trustManager};
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, trustAllCerts, new SecureRandom());
    SSLSocketFactory socketFactory = sslContext.getSocketFactory();
    OkHttpClient client = new OkHttpClient().newBuilder()
        .sslSocketFactory(socketFactory, trustManager)
        .followSslRedirects(false)
        .hostnameVerifier((h,s) -> true)
        .retryOnConnectionFailure(true)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(chain -> {
          Request req = chain.request().newBuilder()
              .addHeader(HttpHeaders.CONTENT_TYPE, JSON.toString())
              .build();
          return chain.proceed(req);
        })
        .addInterceptor(new SignInterceptor(config.getUser(), config.getAuthKey())).build();

    this.retrofit = new Retrofit.Builder()
        .baseUrl(config.getUrl())
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();

    this.torbit = retrofit.create(TorbitApi.class);
  }


  private X509TrustManager getTrustManager() {
    return new X509TrustManager() {

      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(X509Certificate[] certs, String authType) {
      }
    };
  }

  public void setTorbit(TorbitApi torbit) {
    this.torbit = torbit;
  }

  public TorbitApi getTorbit() {
    return torbit;
  }

  public Retrofit getRetrofit() {
    return retrofit;
  }
}
