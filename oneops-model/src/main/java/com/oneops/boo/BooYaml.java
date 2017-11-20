package com.oneops.boo;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BooYaml {

  private Boo boo;
  private BooAssembly assembly;
  private Map<String, BooPlatform> platforms;
  private Map<String, BooEnvironment> environments;
  private BooEnvironment environment;
  private Map<String, Map<String, Map<String, Map<String, String>>>> scale;
  private Map<String, Object> variables;

  public Boo getBoo() {
    return boo;
  }

  public void setBoo(Boo boo) {
    this.boo = boo;
  }

  public BooAssembly getAssembly() {
    return assembly;
  }

  public void setAssembly(BooAssembly assembly) {
    this.assembly = assembly;
  }

  public Map<String, BooPlatform> getPlatforms() {
    return platforms;
  }

  public void setPlatforms(Map<String, BooPlatform> platforms) {
    this.platforms = platforms;
  }

  public Map<String, BooEnvironment> getEnvironments() {
    return environments;
  }

  public void setEnvironments(Map<String, BooEnvironment> environments) {
    this.environments = environments;
  }

  public List<BooPlatform> getPlatformList() {
    List<BooPlatform> platformList = Lists.newArrayList();
    for (Entry<String, BooPlatform> entry : platforms.entrySet()) {
      BooPlatform platform = entry.getValue();
      platform.setName(entry.getKey());
      platformList.add(platform);
    }
    return platformList;
  }


  public Map<String, Map<String, Map<String, Map<String, String>>>> getScale() {
    return scale;
  }

  public void setScale(Map<String, Map<String, Map<String, Map<String, String>>>> scale) {
    this.scale = scale;
  }

  public List<BooEnvironment> getEnvironmentList() {
    List<BooEnvironment> environmentList = Lists.newArrayList();
    if (this.environments != null && this.environments.size() > 0) {
      for (Entry<String, BooEnvironment> entry : environments.entrySet()) {
        BooEnvironment environment = entry.getValue();
        environment.setName(entry.getKey());
        environmentList.add(environment);
      }
    } else {
      this.environment.setName(boo.getEnvironment_name());
      environmentList.add(this.environment);
    }

    return environmentList;
  }

  public void setEnvironment(BooEnvironment environment) {
    this.environment = environment;
  }

  public List<BooScale> getScaleList() {
    List<BooScale> booScaleList = Lists.newArrayList();
    for (Entry<String, Map<String, Map<String, Map<String, String>>>> entry0 : scale.entrySet()) {
      String platformName = entry0.getKey();
      Map<String, Map<String, String>> booScaleMap = entry0.getValue().get("scaling");
      for (Entry<String, Map<String, String>> entry : booScaleMap.entrySet()) {
        Map<String, String> scaleMap = entry.getValue();
        BooScale booScale = new BooScale();
        booScale.setPlatformName(platformName);
        booScale.setComponentName(entry.getKey());
        booScale.setCurrent(scaleMap.get("current"));
        booScale.setMin(scaleMap.get("min"));
        booScale.setMax(scaleMap.get("max"));
        booScaleList.add(booScale);
      }
    }
    return booScaleList;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }
}
