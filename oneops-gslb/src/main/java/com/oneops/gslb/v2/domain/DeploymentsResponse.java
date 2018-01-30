package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeploymentsResponse extends BaseResponse {

  @SerializedName("deployments")
  private List<Deployment> deployments = new ArrayList<Deployment>();

  public DeploymentsResponse links(Links links) {
    this.links = links;
    return this;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public DeploymentsResponse metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public DeploymentsResponse errors(List<ResponseError> errors) {
    this.errors = errors;
    return this;
  }

  public DeploymentsResponse addErrorsItem(ResponseError errorsItem) {
    this.errors.add(errorsItem);
    return this;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }

  public DeploymentsResponse deployments(List<Deployment> deployments) {
    this.deployments = deployments;
    return this;
  }

  public DeploymentsResponse addDeploymentsItem(Deployment deploymentsItem) {
    this.deployments.add(deploymentsItem);
    return this;
  }

  public List<Deployment> getDeployments() {
    return deployments;
  }

  public void setDeployments(List<Deployment> deployments) {
    this.deployments = deployments;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeploymentsResponse apiDeploymentsResponse = (DeploymentsResponse) o;
    return Objects.equals(this.links, apiDeploymentsResponse.links) &&
        Objects.equals(this.metadata, apiDeploymentsResponse.metadata) &&
        Objects.equals(this.errors, apiDeploymentsResponse.errors) &&
        Objects.equals(this.deployments, apiDeploymentsResponse.deployments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, deployments);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeploymentsResponse {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    deployments: ").append(toIndentedString(deployments)).append("\n");
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

