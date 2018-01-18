package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MtdHealthCheckHeader {

  @SerializedName("name")
  private String name = null;

  @SerializedName("value")
  private List<String> value = new ArrayList<String>();

  public MtdHealthCheckHeader name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MtdHealthCheckHeader value(List<String> value) {
    this.value = value;
    return this;
  }

  public MtdHealthCheckHeader addValueItem(String valueItem) {
    this.value.add(valueItem);
    return this;
  }

  public List<String> getValue() {
    return value;
  }

  public void setValue(List<String> value) {
    this.value = value;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MtdHealthCheckHeader apiMTDHealthCheckHeaderRead = (MtdHealthCheckHeader) o;
    return Objects.equals(this.name, apiMTDHealthCheckHeaderRead.name) &&
        Objects.equals(this.value, apiMTDHealthCheckHeaderRead.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MTDHealthCheckHeader {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

