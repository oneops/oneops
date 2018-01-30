package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class SqlNullInt64 {

  @SerializedName("Int64")
  private Long int64 = null;

  @SerializedName("Valid")
  private Boolean valid = null;

  public SqlNullInt64 int64(Long int64) {
    this.int64 = int64;
    return this;
  }

  public Long getInt64() {
    return int64;
  }

  public void setInt64(Long int64) {
    this.int64 = int64;
  }

  public SqlNullInt64 valid(Boolean valid) {
    this.valid = valid;
    return this;
  }

  public Boolean getValid() {
    return valid;
  }

  public void setValid(Boolean valid) {
    this.valid = valid;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SqlNullInt64 sqlNullInt64 = (SqlNullInt64) o;
    return Objects.equals(this.int64, sqlNullInt64.int64) &&
        Objects.equals(this.valid, sqlNullInt64.valid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(int64, valid);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SqlNullInt64 {\n");
    
    sb.append("    int64: ").append(toIndentedString(int64)).append("\n");
    sb.append("    valid: ").append(toIndentedString(valid)).append("\n");
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

