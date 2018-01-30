package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cloud {

  @SerializedName("id")
  private Integer id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("data_center_id")
  private Integer dataCenterId = null;

  @SerializedName("cidrs")
  private List<String> cidrs = new ArrayList<>();

  public Cloud id(Integer id) {
    this.id = id;
    return this;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Cloud name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Cloud dataCenterId(Integer dataCenterId) {
    this.dataCenterId = dataCenterId;
    return this;
  }

  public Integer getDataCenterId() {
    return dataCenterId;
  }

  public void setDataCenterId(Integer dataCenterId) {
    this.dataCenterId = dataCenterId;
  }

  public Cloud cidrs(List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public Cloud addCidrsItem(String cidrsItem) {
    this.cidrs.add(cidrsItem);
    return this;
  }

  public List<String> getCidrs() {
    return cidrs;
  }

  public void setCidrs(List<String> cidrs) {
    this.cidrs = cidrs;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Cloud modelsCloud = (Cloud) o;
    return Objects.equals(this.id, modelsCloud.id) &&
        Objects.equals(this.name, modelsCloud.name) &&
        Objects.equals(this.dataCenterId, modelsCloud.dataCenterId) &&
        Objects.equals(this.cidrs, modelsCloud.cidrs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, dataCenterId, cidrs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Cloud {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    dataCenterId: ").append(toIndentedString(dataCenterId)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
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

