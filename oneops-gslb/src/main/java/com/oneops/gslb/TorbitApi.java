package com.oneops.gslb;

import com.oneops.gslb.v2.domain.AuthStatusResponse;
import com.oneops.gslb.v2.domain.CreateMTDBaseRequest;
import com.oneops.gslb.v2.domain.DataCentersResponse;
import com.oneops.gslb.v2.domain.DeployMTDConfigRequest;
import com.oneops.gslb.v2.domain.DeploymentResponse;
import com.oneops.gslb.v2.domain.MTDBHostsVersion;
import com.oneops.gslb.v2.domain.MTDBHostsVersionRequest;
import com.oneops.gslb.v2.domain.MTDBaseHostRequest;
import com.oneops.gslb.v2.domain.MTDBaseHostResponse;
import com.oneops.gslb.v2.domain.MTDBaseResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface TorbitApi {

  @GET("/api/v2/data-centers")
  Call<DataCentersResponse> getDataCenters();

  @GET("/api/v2/mtds/bases/by-name/{mtdBaseName}")
  Call<MTDBaseResponse> getMTDBase(@Path("mtdBaseName") String mtdBaseName);

  @POST("/api/v2/mtds/bases")
  Call<MTDBaseResponse> createMTDBase(@Body CreateMTDBaseRequest mtdRequest, @Query("group_id") int groupId);

  @DELETE("/api/v2/mtds/bases/{mtdBaseId}")
  Call<MTDBaseResponse> deleteMTDBase(@Path("mtdBaseId") int mtdBaseId);

  @POST("/api/v2/versions/mtd-hosts?include_version=true")
  Call<MTDBHostsVersion> createMTDHostsVersion(@Body MTDBHostsVersionRequest mtdHostRequest);

  @POST("/api/v2/mtds/bases/{mtdBaseId}/hosts")
  Call<MTDBaseHostResponse> createMTDHost(@Body MTDBaseHostRequest mtdHostRequest, @Path("mtdBaseId") int mtdBaseId);

  @PUT("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MTDBaseHostResponse> updateMTDHost(@Body MTDBaseHostRequest mtdHostRequest,
      @Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @DELETE("/api/v2/mtds/bases/{mtdBaseId}/hosts/{hostName}")
  Call<MTDBaseHostResponse> deletetMTDHost(@Path("mtdBaseId") int mtdBaseId, @Path("hostName") String hostName);

  @GET("/api/v2/versions/mtd-hosts/{versionId}")
  Call<MTDBHostsVersion> getMTDHostsVersion(@Path("versionId") long version);

  @POST("/api/v2/deployments/mtd-configs")
  Call<DeploymentResponse> deployMTDConfig(@Body DeployMTDConfigRequest deployMTDRequest);

  @GET("/api/v2/auth/status")
  Call<AuthStatusResponse> authStatus();

}
