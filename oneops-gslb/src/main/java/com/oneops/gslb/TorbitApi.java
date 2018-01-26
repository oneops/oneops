package com.oneops.gslb;

import com.oneops.gslb.v2.domain.AuthStatusResponse;
import com.oneops.gslb.v2.domain.CreateMtdBaseRequest;
import com.oneops.gslb.v2.domain.DataCentersResponse;
import com.oneops.gslb.v2.domain.DeployMtdConfigRequest;
import com.oneops.gslb.v2.domain.DeploymentResponse;
import com.oneops.gslb.v2.domain.MtdHostResponse;
import com.oneops.gslb.v2.domain.MtdbHostsVersion;
import com.oneops.gslb.v2.domain.MtdbHostsVersionRequest;
import com.oneops.gslb.v2.domain.MtdBaseHostRequest;
import com.oneops.gslb.v2.domain.MtdBaseHostResponse;
import com.oneops.gslb.v2.domain.MtdBaseResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface TorbitApi {

  @GET("/api/v2/data-centers")
  Call<DataCentersResponse> getDataCenters();

  @GET("/api/v2/mtds/bases/by-name/{mtdBaseName}")
  Call<MtdBaseResponse> getMTDBase(@Path("mtdBaseName") String mtdBaseName);

  @POST("/api/v2/mtds/bases")
  Call<MtdBaseResponse> createMTDBase(@Body CreateMtdBaseRequest mtdRequest, @Query("group_id") int groupId);

  @DELETE("/api/v2/mtds/bases/{mtdBaseId}")
  Call<MtdBaseResponse> deleteMTDBase(@Path("mtdBaseId") int mtdBaseId);

  @POST("/api/v2/versions/mtd-hosts?include_version=true")
  Call<MtdbHostsVersion> createMTDHostsVersion(@Body MtdbHostsVersionRequest mtdHostRequest);

  @GET("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MtdHostResponse> getMTDHost(@Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @POST("/api/v2/mtds/bases/{mtdBaseId}/hosts")
  Call<MtdBaseHostResponse> createMTDHost(@Body MtdBaseHostRequest mtdHostRequest, @Path("mtdBaseId") int mtdBaseId);

  @PUT("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MtdBaseHostResponse> updateMTDHost(@Body MtdBaseHostRequest mtdHostRequest,
      @Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @DELETE("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MtdBaseHostResponse> deletetMTDHost(@Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @GET("/api/v2/versions/mtd-hosts/{versionId}")
  Call<MtdbHostsVersion> getMTDHostsVersion(@Path("versionId") long version);

  @POST("/api/v2/deployments/mtd-configs")
  Call<DeploymentResponse> deployMTDConfig(@Body DeployMtdConfigRequest deployMTDRequest);

  @GET("/api/v2/auth/status")
  Call<AuthStatusResponse> authStatus();

}
