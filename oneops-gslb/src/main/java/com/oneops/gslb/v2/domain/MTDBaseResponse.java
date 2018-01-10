package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class MTDBaseResponse extends BaseResponse {
  
  @SerializedName("mtd_base")
  private MTDBase mtdBase = null;

  public MTDBaseResponse links(Links links) {
    this.links = links;
    return this;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public MTDBaseResponse metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public MTDBaseResponse errors(List<ResponseError> errors) {
    this.errors = errors;
    return this;
  }

  public MTDBaseResponse addErrorsItem(ResponseError errorsItem) {
    this.errors.add(errorsItem);
    return this;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }

  public MTDBaseResponse mtdBase(MTDBase mtdBase) {
    this.mtdBase = mtdBase;
    return this;
  }

  public MTDBase getMtdBase() {
    return mtdBase;
  }

  public void setMtdBase(MTDBase mtdBase) {
    this.mtdBase = mtdBase;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MTDBaseResponse apiReadMTDBaseResponse = (MTDBaseResponse) o;
    return Objects.equals(this.links, apiReadMTDBaseResponse.links) &&
        Objects.equals(this.metadata, apiReadMTDBaseResponse.metadata) &&
        Objects.equals(this.errors, apiReadMTDBaseResponse.errors) &&
        Objects.equals(this.mtdBase, apiReadMTDBaseResponse.mtdBase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, mtdBase);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDBaseResponse {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    mtdBase: ").append(toIndentedString(mtdBase)).append("\n");
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

