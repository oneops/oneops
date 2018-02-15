package com.oneops.gslb;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneops.gslb.v2.domain.BaseResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ConnectException;
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
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.apache.log4j.Logger;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TorbitClient {

  protected static final MediaType JSON = MediaType.parse("application/json");

  private TorbitApi torbit;

  private Retrofit retrofit;

  private static final Logger logger = Logger.getLogger(TorbitClient.class);

  public TorbitClient(Config config) throws Exception {
    newTorbitApi(config);
  }

  private void newTorbitApi(Config config) throws Exception {
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(AutoValueGsonFactory.create()).create();
    X509TrustManager trustManager = getTrustManager();
    TrustManager[] trustAllCerts = new TrustManager[]{trustManager};
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, trustAllCerts, new SecureRandom());
    SSLSocketFactory socketFactory = sslContext.getSocketFactory();
    HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(m -> logger.info(m));
    logInterceptor.setLevel(Level.BODY);
    OkHttpClient client = new OkHttpClient().newBuilder()
        .sslSocketFactory(socketFactory, trustManager)
        .followSslRedirects(false)
        .hostnameVerifier((h,s) -> true)
        .retryOnConnectionFailure(true)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logInterceptor)
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

  public <T extends BaseResponse> Resp<T> execute(Call<T> call, Class<T> respType) throws IOException, ExecutionException {
    Response<T> response = null;
    try {
      response = call.execute();
    } catch(ConnectException e) {
      throw new ExecutionException("Exception connecting to torbit, check torbit cloud service attributes");
    } catch (Exception e) {
      throw new ExecutionException("Exception calling torbit api " + e.getMessage());
    }

    Resp<T> resp = new Resp<>();
    resp.setSuccessful(response.isSuccessful());
    if (response.isSuccessful()) {
      resp.setBody(response.body());
    }
    else {
      logger.info("response " + response.code() + " message " + response.message());
      failForAuthErrors(response);
      T t = (T) retrofit.responseBodyConverter(respType, new Annotation[0]).convert(response.errorBody());
      resp.setBody(t);
    }
    resp.setCode(response.code());
    return resp;
  }

  private void failForAuthErrors(Response<?> response) throws ExecutionException {
    if (response.code() == 401 || response.code() == 403) {
      throw new ExecutionException("Authentication failed while calling torbit, check torbit cloud service attributes");
    }
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

  public TorbitApi getTorbit() {
    return torbit;
  }

  public Retrofit getRetrofit() {
    return retrofit;
  }
}
