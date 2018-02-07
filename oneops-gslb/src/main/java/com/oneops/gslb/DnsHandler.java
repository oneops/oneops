package com.oneops.gslb;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.infoblox.InfobloxClient;
import com.oneops.infoblox.model.cname.CNAME;
import java.io.IOException;
import java.util.Collection;
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

  private JsonParser jsonParser = new JsonParser();

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
    currentAliases.add(getFullAlias(context.getPlatform(), context));
    addAlias(ciAttributes.get(ATTRIBUTE_ALIAS), currentAliases, t -> (getFullAlias(t, context)));
    addAlias(ciAttributes.get(ATTRIBUTE_FULL_ALIAS), currentAliases, Function.identity());

    if (woHelper.isDeleteAction(wo)) {
      deleteCNames(wo, context, currentAliases, infoBloxClient);
    }
    else {
      Set<String> oldAliases = new HashSet<>();
      Map<String, String> ciBaseAttributes = rfc.getCiBaseAttributes();
      addAlias(ciBaseAttributes.get(ATTRIBUTE_ALIAS), oldAliases, t -> (t + "." + context.getMtdBaseHost()));
      addAlias(ciBaseAttributes.get(ATTRIBUTE_FULL_ALIAS), oldAliases, Function.identity());
      List<String> aliasesToRemove = oldAliases.stream().filter(a -> !currentAliases.contains(a)).collect(Collectors.toList());
      deleteCNames(wo, context, aliasesToRemove, infoBloxClient);
      addOrUpdateCNames(wo, context, currentAliases, infoBloxClient);
    }
  }

  private String getFullAlias(String alias, Context context) {
    return String.join(".", alias, context.getSubdomain(), context.getDnsAttrs().get(ATTRIBUTE_ZONE));
  }

  private void addOrUpdateCNames(CmsWorkOrderSimple wo, Context context, Collection<String> aliases, InfobloxClient infoBloxClient) {
    logger.info(context.getLogKey() + "cnames to be added/updated " + aliases);
    String cname = context.getPlatform() + context.getMtdBaseHost();
    for (String alias : aliases) {
      try {
        List<CNAME> existingCnames = infoBloxClient.getCNameRec(alias);
        if (existingCnames == null || existingCnames.isEmpty()) {
          logger.info(context.getLogKey() + "cname not found, trying to add " + alias);
          CNAME addedCname = infoBloxClient.createCNameRec(alias, cname);
          if (!cname.equals(addedCname.canonical())) {
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
            if (updatedCnames.isEmpty() || !cname.equals(updatedCnames.get(0).canonical())) {
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
