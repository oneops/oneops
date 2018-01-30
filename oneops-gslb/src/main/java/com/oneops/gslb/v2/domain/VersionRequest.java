package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class VersionRequest {

  @SerializedName("based_on")
  private Integer basedOn = null;

  @SerializedName("description")
  private String description = null;

  public VersionRequest basedOn(Integer basedOn) {
    this.basedOn = basedOn;
    return this;
  }

  public Integer getBasedOn() {
    return basedOn;
  }

  public void setBasedOn(Integer basedOn) {
    this.basedOn = basedOn;
  }

  public VersionRequest description(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VersionRequest apiWriteVersion = (VersionRequest) o;
    return Objects.equals(this.basedOn, apiWriteVersion.basedOn) &&
        Objects.equals(this.description, apiWriteVersion.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(basedOn, description);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Version {\n");
    
    sb.append("    basedOn: ").append(toIndentedString(basedOn)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

