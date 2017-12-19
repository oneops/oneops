package com.oneops.cms.dj.domain;

public class RfcHint {
  private String propagation;

	public RfcHint() {
	}

	public RfcHint(String propagation) {
		this.propagation = propagation;
	}

  public String getPropagation() {
    return propagation;
  }

  public void setPropagation(String propagation) {
    this.propagation = propagation;
  }
}
