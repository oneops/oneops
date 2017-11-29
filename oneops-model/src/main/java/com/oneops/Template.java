package com.oneops;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Template {

  private String id;
  private List<String> tags;
  private Map<String, Assertion> assertions;
  private OneOps oneOps;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public Map<String, Assertion> getAssertions() {
    return assertions;
  }

  public void setAssertions(Map<String, Assertion> assertions) {
    this.assertions = assertions;
  }

  public List<Assertion> getAssertionList() {
    List<Assertion> assertionList = Lists.newArrayList();
    for (Entry<String, Assertion> entry : assertions.entrySet()) {
      Assertion assertion = entry.getValue();
      assertion.setId(entry.getKey());
      assertionList.add(assertion);
    }
    return assertionList;
  }

  public OneOps getOneOps() {
    return oneOps;
  }

  public void setOneOps(OneOps oneOps) {
    this.oneOps = oneOps;
  }
}
