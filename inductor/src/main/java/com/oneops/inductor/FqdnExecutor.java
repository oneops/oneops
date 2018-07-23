package com.oneops.inductor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.execution.ComponentWoExecutor;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.simple.domain.Instance;
import com.oneops.gslb.GslbProvider;
import com.oneops.gslb.Status;
import com.oneops.gslb.domain.CloudARecord;
import com.oneops.gslb.domain.DcARecord;
import com.oneops.gslb.domain.Distribution;
import com.oneops.gslb.domain.Gslb;
import com.oneops.gslb.domain.GslbProvisionResponse;
import com.oneops.gslb.domain.GslbResponse;
import com.oneops.gslb.domain.HealthCheck;
import com.oneops.gslb.domain.InfobloxConfig;
import com.oneops.gslb.domain.Lb;
import com.oneops.gslb.domain.Protocol;
import com.oneops.gslb.domain.ProvisionedGslb;
import com.oneops.gslb.domain.TorbitConfig;
import com.oneops.infoblox.InfobloxClient;
import com.oneops.infoblox.model.cname.CNAME;
import com.oneops.infoblox.model.zone.Delegate;
import com.oneops.infoblox.model.zone.ZoneDelegate;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FqdnExecutor implements ComponentWoExecutor {

  private static String ONEOPS_FQDN_CLASS = "bom.oneops.1.Fqdn";
  private static String BASE_FQDN_CLASS = "bom.Fqdn";
  private static final String SERVICE_TYPE_TORBIT = "torbit";
  private static final String SERVICE_TYPE_GDNS = "gdns";
  private static final String SERVICE_TYPE_DNS = "dns";
  private static final String ATTRIBUTE_USER = "user_name";
  private static final String ATTRIBUTE_ENDPOINT = "endpoint";
  private static final String ATTRIBUTE_AUTH_KEY = "auth_key";
  private static final String ATTRIBUTE_GROUP_ID = "group_id";
  private static final String ATTRIBUTE_GSLB_BASE_DOMAIN = "gslb_base_domain";
  private static final String ATTRIBUTE_GDNS = "global_dns";
  private static final String ATTRIBUTE_PLATFORM_ENABLED = "is_platform_enabled";

  static final String ATTRIBUTE_SERVICE_TYPE = "service_type";

  private static final String TORBIT_SERVICE_CLASS = "cloud.service.oneops.1.Torbit";
  private static final String NETSCALAR_SERVICE_CLASS = "cloud.service.Netscaler";
  private static final String PAYLOAD_ENVIRONMENT = "Environment";

  public static final String LB_PAYLOAD = "lb";
  public static final String CLOUDS_PAYLOAD = "fqdnclouds";
  public static final String ATTRIBUTE_DNS_RECORD = "dns_record";
  private static final String ATTRIBUTE_CLOUD_STATUS = "base.Consumes.adminstatus";
  private static final String ATTRIBUTE_CLOUD_PRIORITY = "base.Consumes.priority";
  private static final String ATTRIBUTE_LISTENERS = "listeners";
  private static final String ATTRIBUTE_ECV_MAP = "ecv_map";

  private static final String ATTRIBUTE_DNS_HOST = "host";
  private static final String ATTRIBUTE_DNS_USER_NAME = "username";
  private static final String ATTRIBUTE_DNS_PASSWORD = "password";
  private static final String ATTRIBUTE_DNS_ZONE = "zone";

  private static final String ATTRIBUTE_ALIAS = "aliases";
  private static final String ATTRIBUTE_FULL_ALIAS = "full_aliases";
  private static final String ATTRIBUTE_DISTRIBUTION = "distribution";

  private static final String CLOUD_STATUS_ACTIVE = "active";
  private static final String CLOUD_STATUS_INACTIVE = "inactive";

  private static final String DELEGATION_CONFIG_PATH = "/secrets/gslb_delegation.json";

  private static final Logger logger = Logger.getLogger(FqdnExecutor.class);

  @Autowired WoHelper woHelper;

  @Autowired Gson gson;

  @Autowired FqdnVerifier fqdnVerifier;

  @Autowired JsonParser jsonParser;

  Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

  GslbProvider gslbProvider;

  private Map<String, Distribution> distributionMap;
  private Map<String, Protocol> protocolMap;

  public FqdnExecutor() {
    gslbProvider = new GslbProvider();
    distributionMap = new HashMap<>();
    distributionMap.put("proximity", Distribution.PROXIMITY);
    distributionMap.put("roundrobin", Distribution.ROUND_ROBIN);
    protocolMap = new HashMap<>();
    protocolMap.put("tcp", Protocol.TCP);
    protocolMap.put("http", Protocol.HTTP);
    protocolMap.put("https", Protocol.TCP);
  }

  @Override
  public List<String> getComponentClasses() {
    return Arrays.asList(BASE_FQDN_CLASS, ONEOPS_FQDN_CLASS);
  }

  @Override
  public Response execute(CmsWorkOrderSimple wo, String dataDir) {
    String logKey = woHelper.getLogKey(wo);
    if (isFqdnInstance(wo) && isLocalWo(wo) && isGdnsEnabled(wo) && isTorbitServiceType(wo)) {
      executeInternal(
          wo,
          logKey,
          "deployment",
          dataDir,
          (t, i) -> {
            if (isGslbDeleteAction(wo)) {
              GslbResponse response = gslbProvider.delete(provisionedGslb(wo, t, i, logKey));
              updateWoResult(wo, response, logKey);
            } else {
              GslbProvisionResponse response =
                  gslbProvider.create(getGslbRequestFromWo(wo, t, i, logKey));
              updateWoResult(wo, response, logKey);
            }
          });
      return woHelper.formResponse(wo, logKey);
    }
    logger.info(
        logKey
            + "not executing by FqdnExecutor as these conditions are not met :: "
            + "[fqdn service_type set as torbit && gdns enabled for env && local workorder &&"
            + " torbit cloud service configured]");
    return Response.getNotMatchingResponse();
  }

  private boolean isFqdnInstance(CmsWorkOrderSimpleBase wo) {
    return BASE_FQDN_CLASS.equals(wo.getClassName()) || ONEOPS_FQDN_CLASS.equals(wo.getClassName());
  }

  private boolean isGslbDeleteAction(CmsWorkOrderSimple wo) {
    return woHelper.isDeleteAction(wo) && isPlatformDisabled(wo);
  }

  private ProvisionedGslb provisionedGslb(
      CmsWorkOrderSimple wo,
      TorbitConfig torbitConfig,
      InfobloxConfig infobloxConfig,
      String logKey) {
    Context context = context(wo, infobloxConfig);
    List<String> aliases = new ArrayList<>(getAliasesWithDefault(context));
    List<CloudARecord> cloudEntries = getCloudDnsEntries(context, wo);
    List<DcARecord> dcARecords = getDcDnsEntries(context, wo, logKey);

    return ProvisionedGslb.builder()
        .torbitConfig(torbitConfig)
        .infobloxConfig(infobloxConfig)
        .app(context.platform)
        .subdomain(context.subdomain)
        .cnames(aliases)
        .cloudARecords(cloudEntries)
        .dcARecords(dcARecords)
        .logContextId(logKey)
        .build();
  }

  /**
   * FQDN and GSLB (Torbit/Netscalar) action order execution handler. The action order flow for Fqdn
   * and GSLB is as follows,
   *
   * <p>1. If the action order type is torbit GSLB, the FQDN instance is already deployed as torbit
   * and just execute the torbit GSLB status action regardless of action name. The flow ends here.
   *
   * <p>2. If not (ie, Netscalar) and action is migrate, perform the GSLB migration.
   *
   * <p>3. If action is not migrate, just pass through and let chef execute those recipes.
   *
   * @param ao action order object
   * @param dataDir data directory, used for logging the action order for debugging.
   * @return action order response.
   */
  @Override
  public Response execute(CmsActionOrderSimple ao, String dataDir) {
    String logKey = woHelper.getLogKey(ao);
    // Fqdn torbit service type.
    if (isTorbitGslb(ao)) {
      executeInternal(
          ao,
          logKey,
          "procedure",
          dataDir,
          (t, i) -> {
            GslbResponse response =
                gslbProvider.checkStatus((getGslbRequestFromAo(ao, t, i, logKey)));
            updateAoResult(ao, response, logKey);
          });
      return woHelper.formResponse(ao, logKey);
    }

    // Netscalar gslb migrate action.
    if (isMigrateAction(ao)) {
      return performMigration(ao, dataDir);
    }
    // All non-migrate fqdn actions should go to chef recipes.
    return Response.getNotMatchingResponse();
  }

  /**
   * GSLB migrate action execution.
   *
   * @param ao action order
   * @param dataDir inductor data directory
   * @return action order response.
   */
  private Response performMigration(CmsActionOrderSimple ao, String dataDir) {
    String logKey = woHelper.getLogKey(ao);
    try {

      String fileName = dataDir + "/" + ao.getRecordId() + ".json";
      logger.info(logKey + "Saving migrate action order to file " + fileName);
      writeRequest(gsonPretty.toJson(ao), fileName);

      String baseDomain = gdnsBaseDomain(ao, logKey);
      boolean gdnsEnabled = isGdnsEnabled(ao);
      boolean torbitMigrate = isTorbitMigrate(ao, logKey);
      boolean stickiness = isStickinessEnabled(ao);
      boolean primaryCloud = hasPrimaryCloud(ao);
      boolean ptrEnabled = isPTREnabled(ao);

      TorbitConfig tc = getTorbitConfig(ao, logKey);
      InfobloxConfig ic = getInfobloxConfig(ao);
      if (tc == null || ic == null) {
        throw new IllegalStateException(
            "Torbit/Infoblox config could not be obtained, Please check cloud service configuration.");
      }

      Context context = migrationContext(ao, ic);
      String gslb = platformGslbDomain(context, baseDomain);

      String delegationConfig =
          new String(Files.readAllBytes(Paths.get(DELEGATION_CONFIG_PATH)), UTF_8);
      boolean migratableGslb = isValidDomain(gslb, delegationConfig, logKey);

      String gslbInfo =
          String.format(
              "GSLB fqdn: %s, Migratable GSLB: %s, GSLB base domain: %s, GSLB enabled: %s, Primary Cloud: %s, PTR Enabled: %s",
              gslb, migratableGslb, baseDomain, gdnsEnabled, primaryCloud, ptrEnabled);
      logger.info(logKey + gslbInfo);

      /* Zone delegation infoblox client is used for removing zone delegation records and creating gslb -> torbit alias.*/
      InfobloxClient zoneDelegIbaClient = initZoneDelegationClient(delegationConfig);
      /* Cloud dns infoblox client is used for platform cname operations. */
      InfobloxClient cloudDnsIbaClient = getCloudDNSClient(ic);

      if (gdnsEnabled && torbitMigrate && !stickiness && !ptrEnabled && migratableGslb) {

        logger.info(logKey + "Starting to migrate netscalar gslb to torbit.");
        List<String> aliases = new ArrayList<>(getAliasesWithDefault(context));

        // Holds all newly updated entries.
        Map<String, String> dnsEntries = new HashMap<>();

        // 1. Creating torbit MTD base.
        logger.info(logKey + "The platform gslb domain is " + gslb);
        logger.info(logKey + "Creating torbit MTD base (gdns)...");
        GslbProvisionResponse res = gslbProvider.create(torbitGslbMigrationReq(ao, tc, ic, logKey));
        String torbitGslb = res.getGlb();

        if (res.getStatus() == Status.FAILED) {
          logger.error(logKey + "Torbit MTD base creation failed, " + res.getFailureMessage());
          woHelper.failWo(ao, logKey, res.getFailureMessage(), null);
          return woHelper.formResponse(ao, logKey);
        }
        logger.info(logKey + "Created Torbit MTD base, " + res);

        if (primaryCloud) {
          try {
            logger.info(
                logKey + "Modifying full, short and platform cname to torbit gslb: " + gslb);
            for (String alias : aliases) {
              createOrModifyCName(alias, torbitGslb, cloudDnsIbaClient, logKey);
              dnsEntries.put(alias, torbitGslb);
            }

            // Removing zone delegation record.
            logger.info(logKey + "Deleting zone delegation record for " + gslb);
            List<ZoneDelegate> currDZones = zoneDelegIbaClient.getDelegatedZones(gslb);
            logger.info(logKey + "Zone delegation response " + currDZones);

            if (!currDZones.isEmpty()) {
              // Check for valid zone delegation records.
              if (hasValidDelegationRecords(currDZones.get(0), delegationConfig, logKey)) {
                List<String> zdDelRes = zoneDelegIbaClient.deleteDelegatedZone(gslb);
                logger.info(logKey + "Zone delegation delete response " + zdDelRes);
              } else {
                throw new IllegalStateException(
                    "Found invalid zone delegation records for " + gslb);
              }
            }
            logger.info(logKey + "Zone delegation records deleted for " + gslb);

          } catch (IOException ioe) {
            logger.error(logKey + "Zone delegation operation error for " + gslb, ioe);
            woHelper.failWo(ao, logKey, "Zone delegation operation error for  " + gslb, ioe);
            return woHelper.formResponse(ao, logKey);
          }

          // Adding cname record for gslb to torbit
          createOrModifyCName(gslb, torbitGslb, zoneDelegIbaClient, logKey);
          dnsEntries.put(gslb, torbitGslb);

        } else {
          logger.info(
              logKey + "Not a primary cloud. Skipping zone delegation and cname operations.");
        }

        // Now update the action order response.
        updateGslbMigrateResult(ao, gslb, dnsEntries, primaryCloud, res, logKey);
        logger.info(logKey + "GSLB Migration completed successfully!!");
        return woHelper.formResponse(ao, logKey);

      } else {
        // Unsupported gslb domain.
        String errMsg = "Doesn't meet the GSLB migration prerequisites, " + gslbInfo;
        logger.error(logKey + errMsg);
        woHelper.failWo(ao, logKey, errMsg, null);
        return woHelper.formResponse(ao, logKey);
      }
    } catch (Exception ex) {
      logger.error(logKey + "GSLB migration failed!", ex);
      woHelper.failWo(ao, logKey, "GSLB migration failed!.", ex);
      return woHelper.formResponse(ao, logKey);
    }
  }

  /**
   * Checks if the zone delegate records has valid , allowed <b>delegatesTo</b> record for the base
   * domain.
   *
   * @param zd {@link ZoneDelegate}
   * @param delegationConfig delegation config
   * @param logKey inductor log key
   * @return <code>true</code> if the zone delegation records are valid.
   */
  private boolean hasValidDelegationRecords(
      ZoneDelegate zd, String delegationConfig, String logKey) {

    boolean validDelegations = true;
    JsonObject jo = (JsonObject) gson.fromJson(delegationConfig, JsonElement.class);
    JsonObject configObject = jo.getAsJsonArray("delegation").get(0).getAsJsonObject();

    List<Delegate> actualDelegates = zd.delegateTo();
    List<Delegate> allowedDelegates = new ArrayList<>();
    JsonArray asJsonArray = configObject.getAsJsonArray("delegate_to");
    for (JsonElement jsonElement : asJsonArray) {
      JsonObject dto = jsonElement.getAsJsonObject();
      String address = dto.get("address").getAsString();
      String name = dto.get("name").getAsString();
      allowedDelegates.add(Delegate.of(address, name));
    }

    logger.info(logKey + "Allowed zone delegates are, " + allowedDelegates);
    logger.info(logKey + "Actual zone delegates are, " + actualDelegates);

    for (Delegate ad : actualDelegates) {
      if (!allowedDelegates.contains(ad)) {
        validDelegations = false;
        break;
      }
    }
    return validDelegations;
  }

  /**
   * Update migrate ao with the torbit gslb provision response data.
   *
   * @param ao original action order.
   * @param gslb platform gslb name
   * @param res torbit gslb provision response.
   * @param logKey inductor log key.
   */
  private void updateGslbMigrateResult(
      CmsActionOrderSimple ao,
      String gslb,
      Map<String, String> dnsEntries,
      boolean primaryCloud,
      GslbProvisionResponse res,
      String logKey) {
    if (res != null) {
      if (res.getStatus() == Status.SUCCESS) {
        logger.info(logKey + "Updating the action order results after migration.");
        Map<String, String> mtdMap = new HashMap<>();
        mtdMap.put("mtd_base_id", res.getMtdBaseId());
        mtdMap.put("mtd_version", res.getMtdVersion());
        mtdMap.put("deploy_id", res.getMtdDeploymentId());
        mtdMap.put("glb", res.getGlb());

        Map<String, String> ciAtrrs = ao.getCiAttributes();
        Map<String, String> resultAttrs = woHelper.getResultCiAttributes(ao);

        // Copy all ci attributes to result ci attributes as we need to
        // preserve most of the attributes after migration.
        resultAttrs.putAll(ciAtrrs);

        // Make a copy of entries first as legacy entries
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> newEntries = gson.fromJson(ciAtrrs.get("entries"), type);

        // New entries should be the union of legacy_entries and new aliases.
        // Don't change the entries it's deployed to secondary clouds.
        if (primaryCloud) {
          newEntries.putAll(dnsEntries);
        }

        resultAttrs.put("gslb_map", gson.toJson(mtdMap));
        resultAttrs.put("entries", gson.toJson(newEntries));
        resultAttrs.put("service_type", "torbit");
        resultAttrs.put("legacy_gslb", gslb);
        resultAttrs.put("legacy_entries", ciAtrrs.get("entries"));
        logger.info(logKey + "Migration result ci attributes: " + resultAttrs);

      } else {
        woHelper.failWo(ao, logKey, res.getFailureMessage(), null);
      }
    }
  }

  /**
   * A helper method to create or modify the cname from alias to canonical name using the given
   * infoblox client. This method would do the required checks to make sure it create/update the
   * cname record only when it's necessary.
   *
   * @param aliasName alias name
   * @param canonicalName canonical name
   * @param client infoblox client
   * @param logKey inductor log key
   * @throws IOException throws if any error communicating infoblox.
   */
  private void createOrModifyCName(
      String aliasName, String canonicalName, InfobloxClient client, String logKey)
      throws IOException {
    logger.info(logKey + "Creating cname from " + aliasName + " -> " + canonicalName);
    List<CNAME> cname = client.getCNameRec(aliasName);
    logger.info(logKey + "Current cname record for " + aliasName + " : " + cname);

    if (cname.isEmpty()) {
      logger.info(logKey + "CNAME not exists for " + aliasName + ". Creating new one");
      CNAME cNameRec = client.createCNameRec(aliasName, canonicalName);
      logger.info(logKey + "Created cname : " + cNameRec);
    } else {
      String currCanonicalName = cname.get(0).canonical();
      if (canonicalName.equalsIgnoreCase(currCanonicalName)) {
        logger.info(logKey + "CNAME already exists for " + aliasName + ". Skipping.");
      } else {
        logger.info(logKey + "Modifying the CNAME for " + aliasName);
        List<CNAME> modCNamd = client.modifyCNameCanonicalRec(aliasName, canonicalName);
        logger.info(logKey + "Modified cname: " + modCNamd);
      }
    }
  }

  /**
   * Checks if the gslb domain is valid for migration. The gslb fqdn is valid only if
   *
   * <p>1. It's not equal to migrate base domain
   *
   * <p>2. Should ends with migrate base domain
   *
   * <p>3. Shouldn't contains any of the nested base domains (excluded domains).
   *
   * <p>4. Shouldn't ends with any of the nested base domains.
   *
   * @param gslbDomain gslb fqdn
   * @param delegationConfig delegation config json
   * @param logKey inductor log key
   * @return <code>true</code> if it's a migratable gslb domain.
   */
  protected boolean isValidDomain(String gslbDomain, String delegationConfig, String logKey) {

    JsonObject jo = (JsonObject) gson.fromJson(delegationConfig, JsonElement.class);
    JsonObject configObject = jo.getAsJsonArray("delegation").get(0).getAsJsonObject();

    String migrateBaseDomain = configObject.get("base_domain").getAsString();

    List<String> excludedDomains = new ArrayList<>();
    JsonArray asJsonArray = configObject.getAsJsonArray("excluded_domains");
    for (JsonElement jsonElement : asJsonArray) {
      excludedDomains.add(jsonElement.getAsString());
    }
    logger.info(logKey + "Migration config, base domain: " + migrateBaseDomain);
    logger.info(logKey + "Migration config, excluded domains: " + excludedDomains);

    boolean nestedDomain = false;
    for (String excludedDomain : excludedDomains) {
      if (gslbDomain.equalsIgnoreCase(excludedDomain) || gslbDomain.endsWith(excludedDomain)) {
        nestedDomain = true;
        break;
      }
    }

    return gslbDomain != null
        && gslbDomain.endsWith(migrateBaseDomain)
        && !gslbDomain.equalsIgnoreCase(migrateBaseDomain)
        && !nestedDomain;
  }

  /**
   * Initialize a new zone delegation infoblox client by reading the
   * <b>/secrets/gslb_delegation.json</b> config. This client is solely used for zone delegation
   * records deletion for GSLB. The client timeout is set to zero to handle the zone delegation
   * deletion errors due to timeouts. For GSLB records the default TTL is <b>5 seconds</b>
   *
   * @param delegationConfig delegation config json.
   * @return {@link InfobloxClient}
   * @throws IOException throws if any error accessing infoblox grid or gslb delegation config.
   */
  private InfobloxClient initZoneDelegationClient(String delegationConfig) throws IOException {
    JsonObject jo = (JsonObject) gson.fromJson(delegationConfig, JsonElement.class);
    JsonObject config = jo.getAsJsonObject("infoblox");
    return InfobloxClient.builder()
        .endPoint(config.get("host").getAsString())
        .userName(config.get("username").getAsString())
        .password(config.get("password").getAsString())
        .ttl(5)
        .timeout(0)
        .debug(true)
        .tlsVerify(false)
        .build();
  }

  /**
   * Initialize a cloud fqdn client for creating DNS cname during the migration. All newly created
   * cnames will have a default 60 seconds TTL.
   *
   * @param config cloud infoblox config.
   * @return {@link InfobloxClient}
   */
  private InfobloxClient getCloudDNSClient(InfobloxConfig config) {
    return InfobloxClient.builder()
        .endPoint(config.host())
        .userName(config.user())
        .password(config.pwd())
        .ttl(60)
        .timeout(0)
        .debug(true)
        .tlsVerify(false)
        .build();
  }

  /**
   * Checks if the fqdn instance is deployed to an active/ignored primary cloud.
   *
   * @param ao action order.
   * @return <code>true</code> if the cloud is primary.
   */
  private boolean hasPrimaryCloud(CmsActionOrderSimple ao) {
    Map<Long, Cloud> clouds = getPlatformClouds(ao);
    Instance lb = woHelper.getLbFromDependsOn(ao);

    String lbName = lb.getCiName();
    String[] elements = lbName.split("-");
    String cloudId = elements[elements.length - 2];
    Cloud cloud = clouds.get(Long.parseLong(cloudId));

    return "1".equals(cloud.priority)
        && (CLOUD_STATUS_ACTIVE.equals(cloud.adminStatus)
            || CLOUD_STATUS_INACTIVE.equals(cloud.adminStatus));
  }

  /**
   * Checks if the PTR record is enabled for GSLB.
   *
   * @param ao action order object
   * @return <code>true</code> if the pointer record is enabled.
   */
  private boolean isPTREnabled(CmsActionOrderSimple ao) {
    boolean ptrEnabled = false;
    Map<String, String> fqdnAttrs = ao.getCi().getCiAttributes();
    String ptrAttr = fqdnAttrs.get("ptr_enabled");
    if ("true".equalsIgnoreCase(ptrAttr)) {
      ptrEnabled = true;
    }
    return ptrEnabled;
  }

  /**
   * Checks if user is executing GSLB migration action.
   *
   * @param ao ao action order.
   * @return <code>true</code> if the action name is migrate.
   */
  private boolean isMigrateAction(CmsActionOrderSimple ao) {
    return "migrate".equalsIgnoreCase(ao.getActionName());
  }

  /**
   * Checks if the migration arg is torbit (ie, from netscalar to torbit).
   *
   * @param ao action order.
   * @param logKey inductor log key.
   * @return <code>true</code> if the arg list is <b>torbit</b>
   */
  private boolean isTorbitMigrate(CmsActionOrderSimple ao, String logKey) {
    boolean migrate = false;
    try {
      JsonObject root = (JsonObject) gson.fromJson(ao.getArglist(), JsonElement.class);
      // This is a platform level migrate action.
      if (root.has("migrate")) {
        String arg = root.get("migrate").getAsString();
        logger.info(logKey + "Fqdn migrate action arg is, " + arg);
        migrate = "torbit".equalsIgnoreCase(arg);
      }
    } catch (Exception ignore) {
    }
    return migrate;
  }

  /**
   * Builds new torbit GSLB request for migrating the netscalar gslb fqdn. This request should
   * create only the new torbit gslb A record.
   *
   * @param ao action order request
   * @param torbitConfig config to connect to torbit
   * @param infobloxConfig config to connect to infoblox
   * @param logKey inductor log key
   * @return torbit gslb object.
   */
  private Gslb torbitGslbMigrationReq(
      CmsActionOrderSimple ao,
      TorbitConfig torbitConfig,
      InfobloxConfig infobloxConfig,
      String logKey) {

    Context context = migrationContext(ao, infobloxConfig);
    Map<String, String> fqdnAttrs = ao.getCi().getCiAttributes();

    return Gslb.builder()
        .app(context.platform)
        .subdomain(context.subdomain)
        .distribution(distribution(fqdnAttrs))
        .torbitConfig(torbitConfig)
        .infobloxConfig(infobloxConfig)
        .logContextId(logKey)
        .lbs(lbTargets(ao))
        .healthChecks(healthChecks(context, logKey))
        .cnames(Collections.emptyList())
        .cloudARecords(Collections.emptyList())
        .obsoleteCnames(Collections.emptyList())
        .obsoleteCloudARecords(Collections.emptyList())
        .build();
  }

  /**
   * Returns the platform gslb domain.
   *
   * @param ctx fqdn executor contenxt
   * @param gdnsBaseDomain gdns base domain.
   * @return platform gslb fqdn.
   */
  private String platformGslbDomain(Context ctx, String gdnsBaseDomain) {
    return String.join(".", ctx.platform, ctx.subdomain, gdnsBaseDomain);
  }
  /**
   * Returns the netscalar gdns base domain.
   *
   * @param ao action order.
   * @param logKey inductor log key
   * @return gdns base domain.
   */
  private String gdnsBaseDomain(CmsActionOrderSimple ao, String logKey) {
    String domain = null;
    Map<String, Map<String, CmsCISimple>> services = ao.getServices();
    if (services != null && services.containsKey(SERVICE_TYPE_GDNS)) {
      Map<String, CmsCISimple> gdnsService = services.get(SERVICE_TYPE_GDNS);
      CmsCISimple gdns;
      if ((gdns = gdnsService.get(ao.getCloud().getCiName())) != null) {
        if (NETSCALAR_SERVICE_CLASS.equals(gdns.getCiClassName())) {
          Map<String, String> attributes = gdns.getCiAttributes();
          domain = attributes.get(ATTRIBUTE_GSLB_BASE_DOMAIN);
        }
      }
    } else {
      logger.info(logKey + "Gdns service not found in action order!");
    }
    return domain;
  }

  /**
   * Checks if <b>session persistence (stickiness)</b> is enabled for this action order.
   *
   * @param ao action order
   * @return <code>true</code> if stickiness is enabled.
   */
  private boolean isStickinessEnabled(CmsActionOrderSimple ao) {
    boolean stickiness = false;
    Instance lb = woHelper.getLbFromDependsOn(ao);
    if (lb != null) {
      String stkVal = lb.getCiAttributes().get("stickiness");
      stickiness = "true".equalsIgnoreCase(stkVal);
    }
    return stickiness;
  }

  /**
   * Check if gslb(gdns) enabled for action order.
   *
   * @param ao action order
   * @return <code>true</code> if the GSLB(GDNS) is enabled.
   */
  private boolean isGdnsEnabled(CmsActionOrderSimple ao) {
    if (ao.isPayLoadEntryPresent(PAYLOAD_ENVIRONMENT)) {
      CmsCISimple env = ao.getPayLoadEntryAt(PAYLOAD_ENVIRONMENT, 0);
      Map<String, String> attrs = env.getCiAttributes();
      if (attrs.containsKey(ATTRIBUTE_GDNS)) {
        return "true".equals(attrs.get(ATTRIBUTE_GDNS));
      }
    }
    return false;
  }

  /**
   * Fqdn executor context for migrate action order.
   *
   * @param ao action order.
   * @param ibaConfig infoblox config
   * @return context.
   */
  private Context migrationContext(CmsActionOrderSimple ao, InfobloxConfig ibaConfig) {
    Context context = baseContext(ao);
    context.infobloxConfig = ibaConfig;
    Map<String, List<CmsCISimple>> payload = ao.getPayLoad();
    CmsCISimple env = payload.get("Environment").get(0);
    context.subdomain = env.getCiAttributes().get("subdomain");
    context.lb = woHelper.getLbFromDependsOn(ao);
    Map<String, String> fqdnAttrs = ao.getCi().getCiAttributes();
    context.shortAliases = getShortAliases(fqdnAttrs);
    context.fullAliases = getFullAliases(fqdnAttrs);
    return context;
  }

  private void executeInternal(
      CmsWorkOrderSimpleBase wo,
      String logKey,
      String execType,
      String dataDir,
      BiConsumer<TorbitConfig, InfobloxConfig> consumer) {
    try {
      String fileName = dataDir + "/" + wo.getRecordId() + ".json";
      writeRequest(gsonPretty.toJson(wo), fileName);
      logger.info(logKey + "wo file written to " + fileName);
      TorbitConfig torbitConfig = getTorbitConfig(wo, logKey);
      InfobloxConfig infobloxConfig = getInfobloxConfig(wo);
      if (torbitConfig != null && infobloxConfig != null) {
        logger.info(
            logKey
                + "FqdnExecutor executing "
                + execType
                + " : "
                + wo.getExecutionId()
                + " action : "
                + wo.getAction());
        consumer.accept(torbitConfig, infobloxConfig);
      } else {
        woHelper.failWo(
            wo,
            logKey,
            "Torbit/Infoblox config could not be obtained, Please check cloud service configuration",
            null);
      }
    } catch (Exception e) {
      woHelper.failWo(wo, logKey, "Exception setting up fqdn ", e);
    }
  }

  @Override
  public Response verify(CmsWorkOrderSimple wo, Response response) {
    String logKey = woHelper.getLogKey(wo);
    TorbitConfig torbitConfig = getTorbitConfig(wo, logKey);
    InfobloxConfig infobloxConfig = getInfobloxConfig(wo);
    Response responseAfterVerify;
    if (isGslbDeleteAction(wo)) {
      responseAfterVerify =
          fqdnVerifier.verifyDelete(
              provisionedGslb(wo, torbitConfig, infobloxConfig, logKey), wo, response);
    } else {
      responseAfterVerify =
          fqdnVerifier.verifyCreate(
              getGslbRequestFromWo(wo, torbitConfig, infobloxConfig, logKey), wo, response);
    }
    return responseAfterVerify;
  }

  private void updateWoResult(
      CmsWorkOrderSimple wo, GslbProvisionResponse response, String logKey) {
    if (response != null) {
      if (response.getStatus() == Status.SUCCESS) {
        Map<String, String> resultAttrs = woHelper.getResultCiAttributes(wo);
        Map<String, String> mtdMap = new HashMap<>();
        mtdMap.put("mtd_base_id", response.getMtdBaseId());
        mtdMap.put("mtd_version", response.getMtdVersion());
        mtdMap.put("deploy_id", response.getMtdDeploymentId());
        mtdMap.put("glb", response.getGlb());
        resultAttrs.put("gslb_map", gson.toJson(mtdMap));
        resultAttrs.put("entries", gson.toJson(response.getDnsEntries()));
      } else {
        woHelper.failWo(wo, logKey, response.getFailureMessage(), null);
      }
    }
  }

  private void updateWoResult(CmsWorkOrderSimple wo, GslbResponse response, String logKey) {
    if (response != null) {
      if (response.getStatus() != Status.SUCCESS) {
        woHelper.failWo(wo, logKey, response.getFailureMessage(), null);
      }
    }
  }

  private void updateAoResult(CmsActionOrderSimple wo, GslbResponse response, String logKey) {
    if (response != null) {
      if (response.getStatus() != Status.SUCCESS) {
        woHelper.failWo(wo, logKey, response.getFailureMessage(), null);
      }
    }
  }

  private Gslb getGslbRequestFromWo(
      CmsWorkOrderSimple wo,
      TorbitConfig torbitConfig,
      InfobloxConfig infobloxConfig,
      String logKey) {
    Context context = context(wo, infobloxConfig);
    Map<String, String> fqdnAttrs = wo.getRfcCi().getCiAttributes();
    Map<String, String> fqdnBaseAttrs = wo.getRfcCi().getCiBaseAttributes();

    Set<String> aliases = getAliasesWithDefault(context);
    List<CloudARecord> cloudEntries = getCloudDnsEntries(context, wo);
    List<DcARecord> dcARecords = getDcDnsEntries(context, wo, logKey);
    //TODO: also create cloud-level cnames (using short alias) to the cloud dns entry

    List<String> oldAliases =
        getAliases(context, fqdnBaseAttrs)
            .stream()
            .filter(a -> !aliases.contains(a))
            .collect(Collectors.toList());
    List<CloudARecord> newEntries = null;
    List<CloudARecord> obsoleteEntries = null;
    if (woHelper.isDeleteAction(wo)) {
      obsoleteEntries = cloudEntries;
      //TODO: check if dc entry needs to be deleted
    } else {
      newEntries = cloudEntries;
    }

    Gslb gslb = Gslb.builder()
        .app(context.platform)
        .subdomain(context.subdomain)
        .distribution(distribution(fqdnAttrs))
        .torbitConfig(torbitConfig)
        .infobloxConfig(infobloxConfig)
        .logContextId(logKey)
        .lbs(lbTargets(wo, context))
        .healthChecks(healthChecks(context, logKey))
        .cnames(new ArrayList<>(aliases))
        .cloudARecords(newEntries)
        .dcARecords(dcARecords)
        .obsoleteCnames(oldAliases)
        .obsoleteCloudARecords(obsoleteEntries)
        .build();
    return gslb;
  }

  private Gslb getGslbRequestFromAo(
      CmsActionOrderSimple ao,
      TorbitConfig torbitConfig,
      InfobloxConfig infobloxConfig,
      String logKey) {
    Context context = context(ao, infobloxConfig);
    Map<String, String> fqdnAttrs = ao.getCi().getCiAttributes();

    List<String> aliases = new ArrayList<>(getAliasesWithDefault(context));
    List<CloudARecord> cloudEntries = getCloudDnsEntries(context, ao);
    List<DcARecord> dcARecords = getDcDnsEntries(context, ao, logKey);

    return Gslb.builder()
        .app(context.platform)
        .subdomain(context.subdomain)
        .distribution(distribution(fqdnAttrs))
        .torbitConfig(torbitConfig)
        .infobloxConfig(infobloxConfig)
        .logContextId(logKey)
        .lbs(lbTargets(ao))
        .healthChecks(healthChecks(context, logKey))
        .cnames(aliases)
        .cloudARecords(cloudEntries)
        .dcARecords(dcARecords)
        .build();
  }

  private Set<String> getAliasesWithDefault(Context context) {
    Set<String> currentAliases = new HashSet<>();
    String defaultAlias = getFullAlias(context.platform, context);
    currentAliases.add(defaultAlias);
    addAliases(context, currentAliases);
    return currentAliases;
  }

  private void addAliases(Context context, Set<String> currentAliases) {
    for(String alias : context.shortAliases) {
      currentAliases.add(getFullAlias(alias, context));
    }
    for(String alias : context.fullAliases) {
      currentAliases.add(alias);
    }
  }

  private Set<String> getAliases(Context context, Map<String, String> fqdnAttrs) {
    Set<String> currentAliases = new HashSet<>();
    addAliases(context, fqdnAttrs, currentAliases);
    return currentAliases;
  }

  private void addAliases(
      Context context, Map<String, String> fqdnAttrs, Set<String> currentAliases) {
    String fullAliases = fqdnAttrs.get(ATTRIBUTE_FULL_ALIAS);
    String aliases = fqdnAttrs.get(ATTRIBUTE_ALIAS);
    addAlias(aliases, currentAliases, t -> (getFullAlias(t, context)));
    addAlias(fullAliases, currentAliases, Function.identity());
  }

  private void addAlias(String attrValue, Set<String> aliases, Function<String, String> mapper) {
    if (isNotBlank(attrValue)) {
      JsonArray aliasArray = (JsonArray) jsonParser.parse(attrValue);
      for (JsonElement alias : aliasArray) {
        aliases.add(mapper.apply(alias.getAsString()));
      }
    }
  }

  private String getFullAlias(String alias, Context context) {
    return String.join(".", alias, context.subdomain, context.infobloxConfig.zone());
  }

  private List<CloudARecord> getCloudDnsEntries(Context context, CmsWorkOrderSimpleBase wo) {
    CmsCISimple dnsService = dnsService(wo);
    List<CloudARecord> list = new ArrayList<>();
    if (dnsService != null) {
      Map<String, String> attributes = dnsService.getCiAttributes();
      if (attributes.containsKey("cloud_dns_id")) {
        String cloudDnsId = attributes.get("cloud_dns_id");
        list.add(cloudARecord(context, wo.getCloud(), context.platform, cloudDnsId));
      }
    }
    return list;
  }

  private List<DcARecord> getDcDnsEntries(Context context, CmsWorkOrderSimpleBase wo, String logKey) {
    List<DcARecord> dcARecords = new ArrayList<>();
    CmsCISimple gdnsService = gdnsService(wo);
    //the gslb_site_dns_id is available only in the netscaler gdns service
    //create dc-level dns entry only if the netscaler gdns service is available for now,
    if (gdnsService != null) {
      Map<String, String> attributes = gdnsService.getCiAttributes();
      if (attributes.containsKey("gslb_site_dns_id")) {
        String datacenter = attributes.get("gslb_site_dns_id");
        Instance lb = context.lb;
        if (lb == null) {
          throw new RuntimeException("DependsOn Lb is empty");
        }

        String vnames = lb.getCiAttributes().get("vnames");
        if (isNotBlank(vnames)) {
          Map<String, String> vnameMap = gson.fromJson(vnames, Map.class);
          String dcDnsEntry = String.join(
              ".",
              context.platform,
              context.subdomain,
              datacenter,
              context.infobloxConfig.zone()).toLowerCase();
          String lbVnamePrefix = dcDnsEntry + "-";
          logger.info(logKey + "DC Level dns entry - dcDnsEntry : " + dcDnsEntry + ", lbVnamePrefix: " + lbVnamePrefix);
          for (Entry<String, String> vname : vnameMap.entrySet()) {
            if (vname.getKey().startsWith(lbVnamePrefix)) {
              dcARecords.add(DcARecord.create(vname.getValue(), dcDnsEntry));
              break;
            }
          }
        }
      }
    }
    return dcARecords;
  }

  private CloudARecord cloudARecord(Context context, CmsCISimple cloud, String prefix, String cloudDnsId) {
    return CloudARecord.create(cloud.getCiName(),
        String.join(
            ".",
            prefix,
            context.subdomain,
            cloudDnsId,
            context.infobloxConfig.zone()).toLowerCase());
  }

  private List<Lb> lbTargets(CmsWorkOrderSimple wo, Context context) {
    Map<Long, Cloud> cloudMap = getPlatformClouds(wo);
    List<CmsRfcCISimple> deployedLbs = wo.getPayLoad().get(LB_PAYLOAD);
    if (woHelper.isDeleteAction(wo)) {
      Instance currentLb = context.lb;
      deployedLbs =
          deployedLbs
              .stream()
              .filter(lb -> (lb.getCiId() != currentLb.getCiId()))
              .collect(Collectors.toList());
    }
    if (deployedLbs == null) {
      throw new RuntimeException("Lb payload not available in workorder");
    }
    return deployedLbs
        .stream()
        .filter(lb -> isNotBlank(lb.getCiAttributes().get(ATTRIBUTE_DNS_RECORD)))
        .map(lb -> lbTarget(lb, cloudMap))
        .collect(Collectors.toList());
  }

  private List<Lb> lbTargets(CmsActionOrderSimple ao) {
    Map<Long, Cloud> cloudMap = getPlatformClouds(ao);
    List<CmsCISimple> deployedLbs = ao.getPayLoad().get(LB_PAYLOAD);
    return deployedLbs
        .stream()
        .filter(lb -> isNotBlank(lb.getCiAttributes().get(ATTRIBUTE_DNS_RECORD)))
        .map(lb -> lbTarget(lb, cloudMap))
        .collect(Collectors.toList());
  }

  private Lb lbTarget(Instance lbCi, Map<Long, Cloud> cloudMap) {
    String lbName = lbCi.getCiName();
    String[] elements = lbName.split("-");
    String cloudId = elements[elements.length - 2];
    Cloud cloud = cloudMap.get(Long.parseLong(cloudId));
    return Lb.create(
        cloud.name,
        lbCi.getCiAttributes().get(ATTRIBUTE_DNS_RECORD),
        isEnabledForTraffic(cloud),
        null);
  }

  private boolean isEnabledForTraffic(Cloud cloud) {
    return "1".equals(cloud.priority)
        && (CLOUD_STATUS_ACTIVE.equals(cloud.adminStatus)
            || CLOUD_STATUS_INACTIVE.equals(cloud.adminStatus));
  }

  private InfobloxConfig getInfobloxConfig(CmsWorkOrderSimpleBase wo) {
    Map<String, CmsCISimple> dnsServices = (Map<String, CmsCISimple>) wo.getServices().get(SERVICE_TYPE_DNS);
    if (dnsServices != null) {
      CmsCISimple dns = dnsServices.get(wo.getCloud().getCiName());
      if (dns != null) {
        Map<String, String> attributes = dns.getCiAttributes();
        String host = attributes.get(ATTRIBUTE_DNS_HOST);
        String user = attributes.get(ATTRIBUTE_DNS_USER_NAME);
        String pwd = attributes.get(ATTRIBUTE_DNS_PASSWORD);
        String zone = attributes.get(ATTRIBUTE_DNS_ZONE);
        if (isNotBlank(host) && isNotBlank(user)) {
          return InfobloxConfig.create(host, user, pwd, zone);
        }
      }
    }
    return null;
  }

  private boolean isPlatformDisabled(CmsWorkOrderSimpleBase wo) {
    boolean isPlatformDisabled = false;
    Map<String, String> platformAttributes = wo.getBox().getCiAttributes();
    if (platformAttributes.containsKey(ATTRIBUTE_PLATFORM_ENABLED)) {
      isPlatformDisabled = "false".equals(platformAttributes.get(ATTRIBUTE_PLATFORM_ENABLED));
    }
    return isPlatformDisabled;
  }

  private List<HealthCheck> healthChecks(Context context, String logKey) {
    Instance lb = context.lb;
    if (lb == null) {
      throw new RuntimeException("DependsOn Lb is empty");
    }
    List<HealthCheck> hcList = new ArrayList<>();
    String listenerJson = lb.getCiAttributes().get(ATTRIBUTE_LISTENERS);
    String ecvMapJson = lb.getCiAttributes().get(ATTRIBUTE_ECV_MAP);
    if (isNotBlank(listenerJson) && isNotBlank(ecvMapJson)) {
      JsonElement element = jsonParser.parse(ecvMapJson);
      if (element instanceof JsonObject) {
        JsonObject root = (JsonObject) element;
        Set<Entry<String, JsonElement>> set = root.entrySet();
        Map<Integer, String> ecvMap =
            set.stream()
                .collect(
                    Collectors.toMap(
                        s -> Integer.parseInt(s.getKey()), s -> s.getValue().getAsString()));
        logger.info(logKey + "listeners " + listenerJson);
        JsonArray listeners = (JsonArray) jsonParser.parse(listenerJson);
        listeners.forEach(
            s -> {
              String listener = s.getAsString();
              // listeners are generally in this format 'http <lb-port> http <app-port>', gslb needs
              // to use the lb-port for health checks
              // ecv map is configured as '<app-port> : <ecv-url>', so we need to use the app-port
              // from listener configuration to lookup the ecv config from ecv map
              String[] config = listener.split(" ");
              if (config.length >= 2) {
                String protocol = config[0];
                int lbPort = Integer.parseInt(config[1]);
                int ecvPort = Integer.parseInt(config[config.length - 1]);
                String healthConfig = ecvMap.get(ecvPort);
                if (healthConfig != null) {
                  if ((protocol.equals("http"))) {
                    String path = healthConfig.substring(healthConfig.indexOf(" ") + 1);
                    logger.info(
                        logKey
                            + "healthConfig : "
                            + healthConfig
                            + ", health check configuration, protocol: "
                            + protocol
                            + ", port: "
                            + lbPort
                            + ", path "
                            + path);
                    hcList.add(newHealthCheck(protocol, lbPort, path));
                  } else {
                    logger.info(
                        logKey
                            + "health check configuration, protocol: "
                            + protocol
                            + ", port: "
                            + lbPort);
                    hcList.add(newHealthCheck(protocol, lbPort, null));
                  }
                }
              }
            });
      }
    }
    return hcList;
  }

  private HealthCheck newHealthCheck(String protocol, int port, String testObjectPath) {
    return HealthCheck.builder()
        .protocol(protocolMap.get(protocol))
        .port(port)
        .path(testObjectPath)
        .build();
  }

  private Map<Long, Cloud> getPlatformClouds(CmsWorkOrderSimple wo) {
    System.out.println(wo.getPayLoad().get(CLOUDS_PAYLOAD));
    List<Cloud> clouds = getPlatformClouds(wo.getPayLoad().get(CLOUDS_PAYLOAD));
    return clouds.stream().collect(Collectors.toMap(Cloud::getCiId, Function.identity()));
  }

  private Map<Long, Cloud> getPlatformClouds(CmsActionOrderSimple ao) {
    List<Cloud> clouds = getPlatformClouds(ao.getPayLoad().get(CLOUDS_PAYLOAD));
    return clouds.stream().collect(Collectors.toMap(Cloud::getCiId, Function.identity()));
  }

  private List<Cloud> getPlatformClouds(List<? extends Instance> clouds) {
    return clouds.stream().map(this::cloudFromCi).collect(Collectors.toList());
  }

  private Cloud cloudFromCi(Instance cloudCi) {
    Map<String, String> attrs = cloudCi.getCiAttributes();
    return new Cloud(
        cloudCi.getCiId(),
        cloudCi.getCiName(),
        attrs.get(ATTRIBUTE_CLOUD_PRIORITY),
        attrs.get(ATTRIBUTE_CLOUD_STATUS));
  }

  private boolean isLocalWo(CmsWorkOrderSimpleBase wo) {
    return !wo.isPayLoadEntryPresent("ManagedVia");
  }

  private boolean isTorbitServiceType(CmsWorkOrderSimple wo) {
    CmsRfcCISimple realizedAs = woHelper.getRealizedAs(wo);
    if (realizedAs != null) {
      String serviceType = realizedAs.getCiAttributes().get(ATTRIBUTE_SERVICE_TYPE);
      logger.info(wo.getCiId() + " : fqdn service type  " + serviceType);
      return SERVICE_TYPE_TORBIT.equals(serviceType);
    }
    return false;
  }

  private boolean isGdnsEnabled(CmsWorkOrderSimple wo) {
    if (wo.isPayLoadEntryPresent(PAYLOAD_ENVIRONMENT)) {
      CmsRfcCISimple env = wo.getPayLoadEntryAt(PAYLOAD_ENVIRONMENT, 0);
      Map<String, String> attrs = env.getCiAttributes();
      if (attrs.containsKey(ATTRIBUTE_GDNS)) {
        return "true".equals(attrs.get(ATTRIBUTE_GDNS));
      }
    }
    return false;
  }

  private TorbitConfig getTorbitConfig(CmsWorkOrderSimpleBase wo, String logKey) {
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    if (services != null && services.containsKey(SERVICE_TYPE_TORBIT)) {
      Map<String, CmsCISimple> gdnsService = services.get(SERVICE_TYPE_TORBIT);
      CmsCISimple gdns;
      if ((gdns = gdnsService.get(wo.getCloud().getCiName())) != null) {
        if (TORBIT_SERVICE_CLASS.equals(gdns.getCiClassName())) {
          Map<String, String> attributes = gdns.getCiAttributes();
          if (attributes.containsKey(ATTRIBUTE_ENDPOINT)
              && attributes.containsKey(ATTRIBUTE_AUTH_KEY)
              && attributes.containsKey(ATTRIBUTE_USER)) {
            return TorbitConfig.create(
                attributes.get(ATTRIBUTE_ENDPOINT),
                attributes.get(ATTRIBUTE_USER),
                attributes.get(ATTRIBUTE_AUTH_KEY),
                Integer.parseInt(attributes.get(ATTRIBUTE_GROUP_ID)),
                attributes.get(ATTRIBUTE_GSLB_BASE_DOMAIN));
          }
        }
      }
      logger.info(logKey + "workorder does not have required elements - torbit gdns service");
    } else {
      logger.info(logKey + "torbit service not found in workorder");
    }
    return null;
  }

  /**
   * Checks if the action order is torbit GSLB type.
   *
   * @param ao action order
   * @return <code>true</code> if it's a local action order , fqdn instance type and service type
   *     attribute is <b>torbit</b>.
   */
  private boolean isTorbitGslb(CmsActionOrderSimple ao) {
    CmsCISimple ci = ao.getCi();
    Map<String, String> attributes = ci.getCiAttributes();
    return isFqdnInstance(ao)
        && isLocalWo(ao)
        && attributes.containsKey(ATTRIBUTE_SERVICE_TYPE)
        && SERVICE_TYPE_TORBIT.equals(attributes.get(ATTRIBUTE_SERVICE_TYPE))
        && attributes.containsKey("gslb_map");
  }

  private Distribution distribution(Map<String, String> fqdnAttributes) {
    String dist = fqdnAttributes.get(ATTRIBUTE_DISTRIBUTION);
    if (distributionMap.containsKey(dist)) {
      return distributionMap.get(dist);
    } else return distributionMap.get("proximity");
  }

  private CmsCISimple service(CmsWorkOrderSimpleBase wo, String serviceType) {
    CmsCISimple service = null;
    Map<String, CmsCISimple> services = (Map<String, CmsCISimple>) wo.getServices().get(serviceType);
    if (services != null) {
      service = services.get(wo.getCloud().getCiName());
    }
    return service;
  }

  private CmsCISimple dnsService(CmsWorkOrderSimpleBase wo) {
    return service(wo, SERVICE_TYPE_DNS);
  }

  private CmsCISimple gdnsService(CmsWorkOrderSimpleBase wo) {
    return service(wo, SERVICE_TYPE_GDNS);
  }

  private List<String> getShortAliases(Map<String, String> fqdnAttrs) {
    List<String> list = new ArrayList<>();
    String aliases = fqdnAttrs.get(ATTRIBUTE_ALIAS);
    parseAndAdd(aliases, list);
    return list;
  }

  private void parseAndAdd(String aliases, List<String> list) {
    if (isNotBlank(aliases)) {
      JsonArray aliasArray = (JsonArray) jsonParser.parse(aliases);
      for (JsonElement alias : aliasArray) {
        list.add(alias.getAsString());
      }
    }
  }

  private List<String> getFullAliases(Map<String, String> fqdnAttrs) {
    List<String> list = new ArrayList<>();
    String aliases = fqdnAttrs.get(ATTRIBUTE_FULL_ALIAS);
    parseAndAdd(aliases, list);
    return list;
  }

  private Context context(CmsWorkOrderSimple wo, InfobloxConfig infobloxConfig) {
    Context context = baseContext(wo);
    context.infobloxConfig = infobloxConfig;
    Map<String, List<CmsRfcCISimple>> payload = wo.getPayLoad();
    CmsRfcCISimple env = payload.get("Environment").get(0);
    String org = payload.get("Organization").get(0).getCiName();
    String assembly = payload.get("Assembly").get(0).getCiName();
    String customSubDomain = env.getCiAttributes().get("subdomain");
    context.subdomain =
        isNotBlank(customSubDomain)
            ? customSubDomain
            : String.join(".", env.getCiName(), assembly, org);
    context.lb = woHelper.getLbFromDependsOn(wo);
    Map<String, String> fqdnAttrs = wo.getRfcCi().getCiAttributes();
    context.shortAliases = getShortAliases(fqdnAttrs);
    context.fullAliases = getFullAliases(fqdnAttrs);
    return context;
  }

  private Context context(CmsActionOrderSimple ao, InfobloxConfig infobloxConfig) {
    Context context = baseContext(ao);
    context.infobloxConfig = infobloxConfig;
    Map<String, List<CmsCISimple>> payload = ao.getPayLoad();
    Instance env = payload.get("Environment").get(0);
    String gslbMap = ao.getCi().getCiAttributes().get("gslb_map");
    JsonObject root = (JsonObject) gson.fromJson(gslbMap, JsonElement.class);
    String glb = null;
    if (root.has("glb")) {
      glb = root.get("glb").getAsString();
    }
    if (isNotBlank(glb)) {
      String[] elements = glb.split("\\.");
      String org = elements[3];
      String assembly = elements[2];
      String customSubDomain = env.getCiAttributes().get("subdomain");
      context.subdomain =
          isNotBlank(customSubDomain)
              ? customSubDomain
              : String.join(".", env.getCiName(), assembly, org);
      context.lb = woHelper.getLbFromDependsOn(ao);
      Map<String, String> fqdnAttrs = ao.getCi().getCiAttributes();
      context.shortAliases = getShortAliases(fqdnAttrs);
      context.fullAliases = getFullAliases(fqdnAttrs);
    }
    else {
      throw new RuntimeException("glb value could not be obtained from gslb_map attribute");
    }
    return context;
  }

  private Context baseContext(CmsWorkOrderSimpleBase woBase) {
    Context context = new Context();
    context.platform = woBase.getBox().getCiName();
    return context;
  }

  class Context {
    String platform;
    String subdomain;
    InfobloxConfig infobloxConfig;
    Instance lb;
    List<String> shortAliases = new ArrayList<>();
    List<String> fullAliases = new ArrayList<>();
  }

  class Cloud {
    long ciId;
    String name;
    String priority;
    String adminStatus;

    Cloud(long ciId, String name, String priority, String adminStatus) {
      this.ciId = ciId;
      this.name = name;
      this.priority = priority;
      this.adminStatus = adminStatus;
    }

    long getCiId() {
      return ciId;
    }
  }
}
