package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class MtdBaseHostResponse extends BaseResponse {

  @SerializedName("version")
  private Version version = null;

  @SerializedName("deployment")
  private MtdDeployment deployment = null;

  public MtdBaseHostResponse links(Links links) {
    this.links = links;
    return this;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public MtdBaseHostResponse metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public MtdBaseHostResponse errors(List<ResponseError> errors) {
    this.errors = errors;
    return this;
  }

  public MtdBaseHostResponse addErrorsItem(ResponseError errorsItem) {
    this.errors.add(errorsItem);
    return this;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }

  public MtdBaseHostResponse version(Version version) {
    this.version = version;
    return this;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public MtdBaseHostResponse deployment(MtdDeployment deployment) {
    this.deployment = deployment;
    return this;
  }

  public MtdDeployment getDeployment() {
    return deployment;
  }

  public void setDeployment(MtdDeployment deployment) {
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
    MtdBaseHostResponse mtdBaseHostResponse = (MtdBaseHostResponse) o;
    return Objects.equals(this.links, mtdBaseHostResponse.links) &&
        Objects.equals(this.metadata, mtdBaseHostResponse.metadata) &&
        Objects.equals(this.errors, mtdBaseHostResponse.errors) &&
        Objects.equals(this.version, mtdBaseHostResponse.version) &&
        Objects.equals(this.deployment, mtdBaseHostResponse.deployment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, version, deployment);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDBaseHostResponse {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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

