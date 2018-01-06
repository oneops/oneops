package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuthStatusResponse {

  @SerializedName("links")
  private Links links = null;

  @SerializedName("metadata")
  private Metadata metadata = null;

  @SerializedName("errors")
  private List<ResponseError> errors = new ArrayList<ResponseError>();

  @SerializedName("user")
  private User user = null;

  @SerializedName("client_ip_address")
  private String clientIpAddress = null;

  @SerializedName("authenticated")
  private Boolean authenticated = null;

  @SerializedName("credentials_validators")
  private String credentialsValidators = null;

  public AuthStatusResponse links(Links links) {
    this.links = links;
    return this;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public AuthStatusResponse metadata(Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public AuthStatusResponse errors(List<ResponseError> errors) {
    this.errors = errors;
    return this;
  }

  public AuthStatusResponse addErrorsItem(ResponseError errorsItem) {
    this.errors.add(errorsItem);
    return this;
  }

  public List<ResponseError> getErrors() {
    return errors;
  }

  public void setErrors(List<ResponseError> errors) {
    this.errors = errors;
  }

  public AuthStatusResponse user(User user) {
    this.user = user;
    return this;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public AuthStatusResponse clientIpAddress(String clientIpAddress) {
    this.clientIpAddress = clientIpAddress;
    return this;
  }

  public String getClientIpAddress() {
    return clientIpAddress;
  }

  public void setClientIpAddress(String clientIpAddress) {
    this.clientIpAddress = clientIpAddress;
  }

  public AuthStatusResponse authenticated(Boolean authenticated) {
    this.authenticated = authenticated;
    return this;
  }

  public Boolean getAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated(Boolean authenticated) {
    this.authenticated = authenticated;
  }

  public AuthStatusResponse credentialsValidators(String credentialsValidators) {
    this.credentialsValidators = credentialsValidators;
    return this;
  }

  public String getCredentialsValidators() {
    return credentialsValidators;
  }

  public void setCredentialsValidators(String credentialsValidators) {
    this.credentialsValidators = credentialsValidators;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthStatusResponse apiReadAuthStatusResponse = (AuthStatusResponse) o;
    return Objects.equals(this.links, apiReadAuthStatusResponse.links) &&
        Objects.equals(this.metadata, apiReadAuthStatusResponse.metadata) &&
        Objects.equals(this.errors, apiReadAuthStatusResponse.errors) &&
        Objects.equals(this.user, apiReadAuthStatusResponse.user) &&
        Objects.equals(this.clientIpAddress, apiReadAuthStatusResponse.clientIpAddress) &&
        Objects.equals(this.authenticated, apiReadAuthStatusResponse.authenticated) &&
        Objects.equals(this.credentialsValidators, apiReadAuthStatusResponse.credentialsValidators);
  }

  @Override
  public int hashCode() {
    return Objects.hash(links, metadata, errors, user, clientIpAddress, authenticated, credentialsValidators);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiReadAuthStatusResponse {\n");
    
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    clientIpAddress: ").append(toIndentedString(clientIpAddress)).append("\n");
    sb.append("    authenticated: ").append(toIndentedString(authenticated)).append("\n");
    sb.append("    credentialsValidators: ").append(toIndentedString(credentialsValidators)).append("\n");
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

