package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseResponse {

  @SerializedName("links")
  protected Links links = null;

  @SerializedName("metadata")
  protected Metadata metadata = null;

  @SerializedName("errors")
  protected List<ResponseError> errors = new ArrayList<>();

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }
}
