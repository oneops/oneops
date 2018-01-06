package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataCenter {

  @SerializedName("id")
  private Integer id = null;

  @SerializedName("name")
  private String name = null;

  @SerializedName("clouds")
  private List<Cloud> clouds = new ArrayList<>();

  public DataCenter id(Integer id) {
    this.id = id;
    return this;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DataCenter name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DataCenter clouds(List<Cloud> clouds) {
    this.clouds = clouds;
    return this;
  }

  public DataCenter addCloudsItem(Cloud cloudsItem) {
    this.clouds.add(cloudsItem);
    return this;
  }

  public List<Cloud> getClouds() {
    return clouds;
  }

  public void setClouds(List<Cloud> clouds) {
    this.clouds = clouds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataCenter modelsDataCenter = (DataCenter) o;
    return Objects.equals(this.id, modelsDataCenter.id) &&
        Objects.equals(this.name, modelsDataCenter.name) &&
        Objects.equals(this.clouds, modelsDataCenter.clouds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, clouds);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DataCenter {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    clouds: ").append(toIndentedString(clouds)).append("\n");
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

