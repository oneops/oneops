package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataCentersResponse {

  @SerializedName("links")
  private Links links = null;

  @SerializedName("metadata")
  private Metadata metadata = null;

  @SerializedName("errors")
  private List<ResponseError> errors = new ArrayList<>();

  @SerializedName("data_centers")
  private List<DataCenter> dataCenters = new ArrayList<>();

  public DataCentersResponse links(Links links) {
    this.links = links;
    return this;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public DataCentersResponse metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public DataCentersResponse errors(List<ResponseError> errors) {
    this.errors = errors;
    return this;
  }

  public DataCentersResponse addErrorsItem(ResponseError errorsItem) {
    this.errors.add(errorsItem);
    return this;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }

  public DataCentersResponse dataCenters(List<DataCenter> dataCenters) {
    this.dataCenters = dataCenters;
    return this;
  }

  public DataCentersResponse addDataCentersItem(DataCenter dataCentersItem) {
    this.dataCenters.add(dataCentersItem);
    return this;
  }

  public List<DataCenter> getDataCenters() {
    return dataCenters;
  }

  public void setDataCenters(List<DataCenter> dataCenters) {
    this.dataCenters = dataCenters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataCentersResponse apiReadDataCentersResponse = (DataCentersResponse) o;
    return Objects.equals(this.links, apiReadDataCentersResponse.links) &&
        Objects.equals(this.metadata, apiReadDataCentersResponse.metadata) &&
        Objects.equals(this.errors, apiReadDataCentersResponse.errors) &&
        Objects.equals(this.dataCenters, apiReadDataCentersResponse.dataCenters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, dataCenters);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DataCentersResponse {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    dataCenters: ").append(toIndentedString(dataCenters)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}

