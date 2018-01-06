
package com.oneops.gslb.v2.domain;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

public class DeployMTDConfigRequest {

  @SerializedName("do_not_sync_externally")
  private Boolean doNotSyncExternally = null;

  @SerializedName("deploy")
  private DeployMTDConfig deploy = null;

  public DeployMTDConfigRequest doNotSyncExternally(Boolean doNotSyncExternally) {
    this.doNotSyncExternally = doNotSyncExternally;
    return this;
  }

  public Boolean getDoNotSyncExternally() {
    return doNotSyncExternally;
  }

  public void setDoNotSyncExternally(Boolean doNotSyncExternally) {
    this.doNotSyncExternally = doNotSyncExternally;
  }

  public DeployMTDConfigRequest deploy(DeployMTDConfig deploy) {
    this.deploy = deploy;
    return this;
  }

  public DeployMTDConfig getDeploy() {
    return deploy;
  }

  public void setDeploy(DeployMTDConfig deploy) {
    this.deploy = deploy;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployMTDConfigRequest apiDeployMTDConfigRequest = (DeployMTDConfigRequest) o;
    return Objects.equals(this.doNotSyncExternally, apiDeployMTDConfigRequest.doNotSyncExternally) &&
        Objects.equals(this.deploy, apiDeployMTDConfigRequest.deploy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doNotSyncExternally, deploy);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiDeployMTDConfigRequest {\n");
    
    sb.append("    doNotSyncExternally: ").append(toIndentedString(doNotSyncExternally)).append("\n");
    sb.append("    deploy: ").append(toIndentedString(deploy)).append("\n");
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

