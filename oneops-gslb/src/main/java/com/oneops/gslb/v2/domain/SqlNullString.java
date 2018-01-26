package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class SqlNullString {

  @SerializedName("String")
  private String string = null;

  @SerializedName("Valid")
  private Boolean valid = null;

  public SqlNullString string(String string) {
    this.string = string;
    return this;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public SqlNullString valid(Boolean valid) {
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
    SqlNullString sqlNullString = (SqlNullString) o;
    return Objects.equals(this.string, sqlNullString.string) &&
        Objects.equals(this.valid, sqlNullString.valid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(string, valid);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SqlNullString {\n");
    
    sb.append("    string: ").append(toIndentedString(string)).append("\n");
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

