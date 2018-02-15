package com.oneops.gslb;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.infoblox.InfobloxClient;
import com.oneops.infoblox.model.a.ARec;
import com.oneops.infoblox.model.cname.CNAME;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DnsHandler {

  @Autowired
  WoHelper woHelper;

  @Autowired
  JsonParser jsonParser;

  @Autowired
  Gson gson;

  private static final Logger logger = Logger.getLogger(DnsHandler.class);

  private static final String ATTRIBUTE_ALIAS = "aliases";
  private static final String ATTRIBUTE_FULL_ALIAS = "full_aliases";
  private static final String ATTRIBUTE_ZONE = "zone";

  private static final String ATTRIBUTE_HOST = "host";
  private static final String ATTRIBUTE_USER_NAME = "username";
  private static final String ATTRIBUTE_PASSWORD = "password";

  private InfobloxClient getInfoBloxClient(CmsWorkOrderSimple wo, Context context) throws ExecutionException {
    Map<String, String> attributes = context.getDnsAttrs();
    String host = attributes.get(ATTRIBUTE_HOST);
    String user = attributes.get(ATTRIBUTE_USER_NAME);
    String pwd = attributes.get(ATTRIBUTE_PASSWORD);
    InfobloxClient client;
    if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(user)) {
      client = InfobloxClient.builder().
          endPoint(host).
          userName(user).
          password(pwd).
          tlsVerify(false).build();
    }
    else {
      throw new ExecutionException("Infoblox client could not be initialized. check cloud service configuration");
    }
    return client;
  }

  public void setupCNames(CmsWorkOrderSimple wo, Context context) {
    logger.info(context.getLogKey() + "setting up cnames");
    InfobloxClient infoBloxClient;
    try {
      setDnsAttributes(wo, context);
      infoBloxClient = getInfoBloxClient(wo, context);
    } catch (ExecutionException e) {
      woHelper.failWo(wo, context.getLogKey(), "Failed while initializing infoblox client", e);
      return;
    }
    CmsRfcCISimple rfc = wo.getRfcCi();

    Set<String> currentAliases = new HashSet<>();
    Map<String, String> ciAttributes = rfc.getCiAttributes();
    String defaultAlias = getFullAlias(context.getPlatform(), context);
    currentAliases.add(defaultAlias);

    addAlias(ciAttributes.get(ATTRIBUTE_ALIAS), currentAliases, t -> (getFullAlias(t, context)));
    addAlias(ciAttributes.get(ATTRIBUTE_FULL_ALIAS), currentAliases, Function.identity());

    if (woHelper.isDeleteAction(wo)) {
      if (context.isPlatformDisabled()) {
        logger.info(context.getLogKey() + "deleting all cnames as platform is getting disabled");
        deleteCNames(wo, context, currentAliases, infoBloxClient);
      }
      else {
        logger.info(context.getLogKey() + "platform is not disabled, deleting only cloud cname");
      }
      deleteCloudEntry(wo, context, infoBloxClient);
    }
    else {
      Set<String> oldAliases = new HashSet<>();
      Map<String, String> ciBaseAttributes = rfc.getCiBaseAttributes();
      addAlias(ciBaseAttributes.get(ATTRIBUTE_ALIAS), oldAliases, t -> (t + "." + context.getMtdBaseHost()));
      addAlias(ciBaseAttributes.get(ATTRIBUTE_FULL_ALIAS), oldAliases, Function.identity());
      List<String> aliasesToRemove = oldAliases.stream().filter(a -> !currentAliases.contains(a)).collect(Collectors.toList());
      deleteCNames(wo, context, aliasesToRemove, infoBloxClient);
      Map<String, String> entriesMap = new HashMap<>();
      addOrUpdateCNames(wo, context, currentAliases, infoBloxClient, entriesMap);
      addOrUpdateCloudEntry(wo, context, infoBloxClient, entriesMap);
      if (!woHelper.isFailed(wo)) {
        updateWoResult(wo, entriesMap, context);
      }
    }
  }

  private void deleteCloudEntry(CmsWorkOrderSimple wo, Context context, InfobloxClient infobloxClient) {
    String cloudEntry = getCloudDnsEntry(wo, context);
    logger.info(context.getLogKey() + "deleting cloud dns entry " + cloudEntry);
    try {
      infobloxClient.deleteARec(cloudEntry);
    } catch(Exception e) {
      woHelper.failWo(wo, context.getLogKey(),"Exception while deleting cloud dns entry ", e);
    }
  }

  private void addOrUpdateCloudEntry(CmsWorkOrderSimple wo, Context context,
      InfobloxClient infobloxClient, Map<String, String> entriesMap) {
    String cloudEntry = getCloudDnsEntry(wo, context);
    CmsRfcCISimple lb = woHelper.getLbFromDependsOn(wo);
    String lbVip = lb.getCiAttributes().get(MtdHandler.ATTRIBUTE_DNS_RECORD);
    logger.info(context.getLogKey() + "cloud dns entry " + cloudEntry + " lbVip " + lbVip);
    if (StringUtils.isNotBlank(lbVip)) {
      entriesMap.put(cloudEntry, lbVip);
      boolean alreadyExists = false;
      try {
        List<ARec> records = infobloxClient.getARec(cloudEntry);
        if (records != null && records.size() == 1) {
          alreadyExists = true;
          if (lbVip.equals(records.get(0).ipv4Addr())) {
            logger.info(context.getLogKey() + " cloud dns entry is already set, not doing anything");
            return;
          }
        }
      } catch (IOException e) {
        woHelper.failWo(wo, context.getLogKey(),"Exception while getting cloud dns entry ", e);
      }
      try {
        if (alreadyExists) {
          logger.info(context.getLogKey() + "updating cloud dns entry ");
          infobloxClient.modifyARec(cloudEntry, lbVip);
        }
        else {
          try {
            logger.info(context.getLogKey() + "creating cloud dns entry ");
            infobloxClient.createARec(cloudEntry, lbVip);
          } catch (Exception e) {
            logger.error(context.getLogKey() + "Exception while creating cloud dns entry : " +  e.getMessage(), e);
            logger.info(context.getLogKey() + "trying to update cloud dns entry");
            infobloxClient.modifyARec(cloudEntry, lbVip);
          }
        }
      } catch (IOException e1) {
        woHelper.failWo(wo, context.getLogKey(), "Exception while updating cloud dns entry ", e1);
      }
    }

  }

  private void updateWoResult(CmsWorkOrderSimple wo, Map<String, String> entriesMap, Context context) {
    Map<String, String> resultAttrs = woHelper.getResultCiAttributes(wo);
    String domainName = context.getPlatform() + context.getMtdBaseHost();
    entriesMap.put(domainName, context.getPrimaryTargets() != null ? context.getPrimaryTargets().toString() : "");
    resultAttrs.put("entries", gson.toJson(entriesMap));
  }

  private String getFullAlias(String alias, Context context) {
    return String.join(".", alias, context.getSubdomain(), context.getDnsAttrs().get(ATTRIBUTE_ZONE));
  }

  private String getCloudDnsEntry(CmsWorkOrderSimple wo, Context context) {
    return String.join(".", context.getPlatform(), context.getSubdomain(),
        context.getCloud(), context.getDnsAttrs().get(ATTRIBUTE_ZONE));
  }

  private void addOrUpdateCNames(CmsWorkOrderSimple wo, Context context,
      Collection<String> aliases, InfobloxClient infoBloxClient, Map<String, String> entriesMap) {
    logger.info(context.getLogKey() + "cnames to be added/updated " + aliases);
    String cname = context.getPlatform() + context.getMtdBaseHost();
    for (String alias : aliases) {
      try {
        entriesMap.put(alias, cname);
        List<CNAME> existingCnames = infoBloxClient.getCNameRec(alias);
        if (existingCnames == null || existingCnames.isEmpty()) {
          logger.info(context.getLogKey() + "cname not found, trying to add " + alias);
          CNAME newCname = null;
          try {
            newCname = infoBloxClient.createCNameRec(alias, cname);
          } catch (IOException e) {
            logger.error(context.getLogKey() + "cname [" + alias + "] creation failed with " + e.getMessage());
            logger.info(context.getLogKey() + "trying to udpate cname record");
            List<CNAME> updatedCnames = infoBloxClient.modifyCNameRec(alias, cname);
            if (updatedCnames != null && !updatedCnames.isEmpty()) {
              newCname = updatedCnames.get(0);
            }
          }
          if (newCname == null || !cname.equals(newCname.canonical())) {
            woHelper.failWo(wo, context.getLogKey(), "Failed to create cname ", null);
            break;
          }
          else {
            logger.info(context.getLogKey() + "cname added successfully");
          }
        }
        else {
          if (existingCnames.size() > 1 || cname.equals(existingCnames.get(0))) {
            logger.info(context.getLogKey() + "cname for " + alias + " found already with different alias " + existingCnames + ", trying to update");
            List<CNAME> updatedCnames = infoBloxClient.modifyCNameRec(alias, cname);
            if (updatedCnames == null || updatedCnames.isEmpty() || !cname.equals(updatedCnames.get(0).canonical())) {
              woHelper.failWo(wo, context.getLogKey(), "Failed to update cname ", null);
              break;
            }
            else {
              logger.info(context.getLogKey() + "cname updated successfully");
            }
          }
          else {
            logger.info(context.getLogKey() + "cname already exists, no change needed " + alias);
          }
        }
      } catch (IOException e) {
        woHelper.failWo(wo, context.getLogKey(), "Failed while updating cnames ", e);
      }
    }
  }

  private void deleteCNames(CmsWorkOrderSimple wo, Context context, Collection<String> aliases, InfobloxClient infoBloxClient) {
    logger.info(context.getLogKey() + "delete cnames " + aliases);
    aliases.stream().forEach(
        a -> {
          try {
            infoBloxClient.deleteCNameRec(a);
          } catch(Exception e) {
            woHelper.failWo(wo, context.getLogKey(), "Failed while deleting cnames ", e);
          }
        }
    );
  }

  private void addAlias(String attrValue, Set<String> aliases, Function<String, String> mapper) {
    if (isNotEmpty(attrValue)) {
      JsonArray aliasArray = (JsonArray) jsonParser.parse(attrValue);
      for (JsonElement alias : aliasArray) {
        aliases.add(mapper.apply(alias.getAsString()));
      }
    }
  }

  private void setDnsAttributes(CmsWorkOrderSimple wo, Context context) throws ExecutionException {
    Map<String, CmsCISimple> dnsServices = wo.getServices().get("dns");
    if (dnsServices != null) {
      CmsCISimple dns = dnsServices.get(context.getCloud());
      if (dns != null) {
        Map<String, String> attributes = dns.getCiAttributes();
        context.setDnsAttrs(attributes);
      }
    }
    if (context.getDnsAttrs() == null) {
      throw new ExecutionException("Infoblox client could not be initialized. check cloud service configuration");
    }
  }

}
