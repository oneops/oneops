package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class DeploymentResponse extends BaseResponse {

  @SerializedName("deployment")
  private Deployment deployment = null;

  public DeploymentResponse links(Links links) {
    this.links = links;
    return this;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public DeploymentResponse metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public DeploymentResponse errors(List<ResponseError> errors) {
    this.errors = errors;
    return this;
  }

  public DeploymentResponse addErrorsItem(ResponseError errorsItem) {
    this.errors.add(errorsItem);
    return this;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }

  public DeploymentResponse deployment(Deployment deployment) {
    this.deployment = deployment;
    return this;
  }

  public Deployment getDeployment() {
    return deployment;
  }

  public void setDeployment(Deployment deployment) {
    this.deployment = deployment;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeploymentResponse apiDeploymentResponse = (DeploymentResponse) o;
    return Objects.equals(this.links, apiDeploymentResponse.links) &&
        Objects.equals(this.metadata, apiDeploymentResponse.metadata) &&
        Objects.equals(this.errors, apiDeploymentResponse.errors) &&
        Objects.equals(this.deployment, apiDeploymentResponse.deployment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, deployment);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeploymentResponse {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    deployment: ").append(toIndentedString(deployment)).append("\n");
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

