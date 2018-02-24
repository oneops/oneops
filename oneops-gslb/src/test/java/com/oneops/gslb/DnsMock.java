package com.oneops.gslb;

import com.oneops.infoblox.InfobloxClient;
import com.oneops.infoblox.model.a.ARec;
import com.oneops.infoblox.model.cname.CNAME;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DnsMock extends InfobloxClient {

  Map<String, String> existingCnames = new HashMap<>();
  Map<String, String> existingARecs = new HashMap<>();
  Map<String, String> newCnames = new HashMap<>();
  Map<String, String> newArecs = new HashMap<>();
  List<String> deleteCnames = new ArrayList<>();
  List<String> deleteArecs = new ArrayList<>();


  @Override
  public List<ARec> getARec(String domainName) throws IOException {
    String val;
    return ((val = existingARecs.get(domainName)) != null) ? Collections.singletonList(ARec.create("", val, domainName, null)) : Collections.emptyList();
  }

  @Override
  public ARec createARec(String domainName, String ipv4Address) throws IOException {
    newArecs.put(domainName, ipv4Address);
    return ARec.create("", ipv4Address, domainName, null);
  }

  @Override
  public List<ARec> modifyARec(String domainName, String newDomainName) throws IOException {
    return Collections.singletonList(createARec(domainName, newDomainName));
  }

  @Override
  public List<CNAME> getCNameRec(String aliasName) throws IOException {
    String val;
    return ((val = existingCnames.get(aliasName)) != null) ? Collections.singletonList(CNAME.create("", val, aliasName, null)) : Collections.emptyList();
  }

  @Override
  public CNAME createCNameRec(String aliasName, String canonicalName) throws IOException {
    newCnames.put(aliasName, canonicalName);
    return CNAME.create("", canonicalName, aliasName, null);
  }

  @Override
  public List<String> deleteCNameRec(String aliasName) throws IOException {
    deleteCnames.add(aliasName);
    return Collections.singletonList(aliasName);
  }

  @Override
  public List<String> deleteARec(String domainName) throws IOException {
    deleteArecs.add(domainName);
    return Collections.singletonList(domainName);
  }

  @Override
  public List<CNAME> modifyCNameRec(String aliasName, String newAliasName) throws IOException {
    return Collections.singletonList(createCNameRec(aliasName, newAliasName));
  }

  @Override
  public String endPoint() {
    return null;
  }

  @Override
  public String wapiVersion() {
    return null;
  }

  @Override
  public String userName() {
    return null;
  }

  @Override
  public String password() {
    return null;
  }

  @Override
  public String dnsView() {
    return null;
  }

  @Override
  public boolean tlsVerify() {
    return false;
  }

  @Override
  public boolean debug() {
    return false;
  }

  @Override
  public int timeout() {
    return 0;
  }

  public Map<String, String> getNewCnames() {
    return newCnames;
  }

  public Map<String, String> getNewArecs() {
    return newArecs;
  }

  public List<String> getDeleteCnames() {
    return deleteCnames;
  }

  public List<String> getDeleteArecs() {
    return deleteArecs;
  }



}
