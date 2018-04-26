package com.oneops.cms.simple.domain;

import java.util.Map;

public interface Instance {

  long getCiId();

  String getCiName();

  String getCiClassName();

  Map<String, String> getCiAttributes();

}
