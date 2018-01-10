package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class MTDBHostsVersion extends BaseResponse {

  @SerializedName("version")
  private Version version = null;

  @SerializedName("mtd_hosts")
  private MTDHosts mtdHosts = null;

  public MTDBHostsVersion links(Links links) {
    this.links = links;
    return this;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public MTDBHostsVersion metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public MTDBHostsVersion errors(List<ResponseError> errors) {
    this.errors = errors;
    return this;
  }

  public MTDBHostsVersion addErrorsItem(ResponseError errorsItem) {
    this.errors.add(errorsItem);
    return this;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }

  public MTDBHostsVersion version(Version version) {
    this.version = version;
    return this;
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public MTDBHostsVersion mtdHosts(MTDHosts mtdHosts) {
    this.mtdHosts = mtdHosts;
    return this;
  }

  public MTDHosts getMtdHosts() {
    return mtdHosts;
  }

  public void setMtdHosts(MTDHosts mtdHosts) {
    this.mtdHosts = mtdHosts;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MTDBHostsVersion mtdbHostsVersion = (MTDBHostsVersion) o;
    return Objects.equals(this.links, mtdbHostsVersion.links) &&
        Objects.equals(this.metadata, mtdbHostsVersion.metadata) &&
        Objects.equals(this.errors, mtdbHostsVersion.errors) &&
        Objects.equals(this.version, mtdbHostsVersion.version) &&
        Objects.equals(this.mtdHosts, mtdbHostsVersion.mtdHosts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, version, mtdHosts);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDBHostsVersion {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    mtdHosts: ").append(toIndentedString(mtdHosts)).append("\n");
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

