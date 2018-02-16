package com.oneops.gslb;

import com.oneops.gslb.v2.domain.AuthStatusResponse;
import com.oneops.gslb.v2.domain.CreateMtdBaseRequest;
import com.oneops.gslb.v2.domain.DataCentersResponse;
import com.oneops.gslb.v2.domain.MtdBaseHostRequest;
import com.oneops.gslb.v2.domain.MtdBaseHostResponse;
import com.oneops.gslb.v2.domain.MtdBaseResponse;
import com.oneops.gslb.v2.domain.MtdHostResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TorbitApi {

  @GET("/api/v2/data-centers")
  Call<DataCentersResponse> getDataCenters();

  @GET("/api/v2/mtds/bases/by-name/{mtdBaseName}")
  Call<MtdBaseResponse> getMTDBase(@Path("mtdBaseName") String mtdBaseName);

  @POST("/api/v2/mtds/bases")
  Call<MtdBaseResponse> createMTDBase(@Body CreateMtdBaseRequest mtdRequest, @Query("group_id") int groupId);

  @DELETE("/api/v2/mtds/bases/{mtdBaseId}")
  Call<MtdBaseResponse> deleteMTDBase(@Path("mtdBaseId") int mtdBaseId);

  @GET("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MtdHostResponse> getMTDHost(@Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @POST("/api/v2/mtds/bases/{mtdBaseId}/hosts")
  Call<MtdBaseHostResponse> createMTDHost(@Body MtdBaseHostRequest mtdHostRequest, @Path("mtdBaseId") int mtdBaseId);

  @PUT("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MtdBaseHostResponse> updateMTDHost(@Body MtdBaseHostRequest mtdHostRequest,
      @Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @DELETE("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MtdBaseHostResponse> deletetMTDHost(@Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @GET("/api/v2/auth/status")
  Call<AuthStatusResponse> authStatus();

}
