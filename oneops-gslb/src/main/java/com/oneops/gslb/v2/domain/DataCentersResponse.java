package com.oneops.gslb.v2.domain;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class DataCentersResponse extends BaseResponse {

  @SerializedName("data_centers")
  @Nullable
  public abstract List<DataCenter> dataCenters();

  public static DataCentersResponse create(Links links, Metadata metadata, List<ResponseError> errors, List<DataCenter> dataCenters) {
    return new AutoValue_DataCentersResponse(links, metadata, errors, dataCenters);
  }

  public static TypeAdapter<DataCentersResponse> typeAdapter(Gson gson) {
    return new AutoValue_DataCentersResponse.GsonTypeAdapter(gson);
  }
  
}

