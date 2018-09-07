/*******************************************************************************
 *

 *   Copyright 2017 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
package com.oneops.crawler;

import static com.oneops.crawler.jooq.crawler.Tables.*;
import static com.oneops.crawler.jooq.cms.Tables.*;

import com.google.gson.Gson;
import com.oneops.Cloud;
import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.jooq.cms.Sequences;
import com.oneops.crawler.plugins.hadr.PlatformHADRCrawlerPlugin;
import com.oneops.crawler.plugins.quota.OneOpsPlatformScaleDownPlugin;
import com.oneops.crawler.plugins.ttl.EnvTTLCrawlerPlugin;
import org.apache.commons.lang.math.NumberUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CMSCrawler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    String crawlerDbUserName;
    String crawlerDbPassword;
    String crawlerDbUrl;

    //cms db details below
    String cmsDbUserName;
    String cmsDbPassword;
    String cmsDbUrl;

    String esUrl;
    ArrayList<Integer> coresAttributeIds = new ArrayList<>();
    ArrayList<Integer> computeClassIds = new ArrayList<>();
    long platformEnabledAttributeId = 0;

    boolean fullSweepAtStart = true;
    boolean shutDownRequested = false;
    boolean syncClouds = false;
    int crawlFrequencyHours = 6;
    boolean singleRun;

    Gson gson= new Gson();
    Map<String, Integer> baseOrganizationMDClassAttributes_NameIdMapCache;

    //TODO: auto-discover plugins from jars
    EnvTTLCrawlerPlugin ttlPlugin = new EnvTTLCrawlerPlugin();
    PlatformHADRCrawlerPlugin platformHADRCrawlerPlugin = new PlatformHADRCrawlerPlugin();
    OneOpsPlatformScaleDownPlugin scaleDownPlugin = new OneOpsPlatformScaleDownPlugin();

    public CMSCrawler() {
        //read and init the secrets
        readConfig();
    }

    private void readConfig() {
        readSystemProperties();
        String secretsPropertiesFilePath = System.getProperty("secrets.properties.file",
                "/secrets/crawler_secrets.properties");
        File secretsFile = new File(secretsPropertiesFilePath);

        if (secretsFile.exists()) {
            try {
                readSecrets(secretsFile);
            } catch (IOException e) {
                log.error("Could not read secrets properties", e);
            }
        }
    }

    private void readSystemProperties() {
        log.info("reading properties..");
        crawlerDbUserName = System.getProperty("crawler.db.user.name");
        crawlerDbPassword = System.getProperty("crawler.db.user.password");
        crawlerDbUrl = System.getProperty("crawler.db.url");

        cmsDbUserName = System.getProperty("cms.db.user.name");
        cmsDbPassword = System.getProperty("cms.db.user.password");
        cmsDbUrl = System.getProperty("cms.db.url");

        String cloudSyncEnabledProperty = System.getProperty("crawler.cloud.metadata.sync.enabled");
        if ("true".equalsIgnoreCase(cloudSyncEnabledProperty)) {
            syncClouds = true;
        }

        String frequencyProperty = System.getProperty("crawler.freqency.hrs", "6");
        crawlFrequencyHours = Integer.parseInt(frequencyProperty);

        String singleRunProperty = System.getProperty("crawler.single.run", "false");
        singleRun = Boolean.parseBoolean(singleRunProperty);
    }

    private void readSecrets(File secretsFile) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(secretsFile));

        crawlerDbUserName = props.getProperty("crawler.db.user.name");
        crawlerDbPassword = props.getProperty("crawler.db.user.password");
        crawlerDbUrl = props.getProperty("crawler.db.url");

        cmsDbUserName = props.getProperty("cms.db.user.name");
        cmsDbPassword = props.getProperty("cms.db.user.password");
        cmsDbUrl = props.getProperty("cms.db.url");
        configurePlugins(props);
    }

    private void configurePlugins(Properties props) {
        scaleDownPlugin.configureSecrets(props);
    }

    public void crawl() {
        long startTimeMillis = System.currentTimeMillis();

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        try (Connection conn = DriverManager.getConnection(cmsDbUrl, cmsDbUserName, cmsDbPassword)) {
            init(conn);
            List<Environment> envs = getOneopsEnvironments(conn);
            Map<String, Organization> organizationsMapCache = populateOrganizations(conn);// caching organizations data
            long envsLastFetchedAt = System.currentTimeMillis();

            while (true && !shutDownRequested) {
                if ((System.currentTimeMillis() - envsLastFetchedAt)/(1000 * 60 * 60 * 24) >= 1 ) { //been a day
                    envs = getOneopsEnvironments(conn);//refresh the environment list
                    organizationsMapCache = populateOrganizations(conn);// refreshing cache
                    envsLastFetchedAt = System.currentTimeMillis();
                }
                ttlPlugin.cleanup(); //from previous run
                log.info("Starting to crawl all environments.. Total # " + envs.size());
                for (Environment env : envs) {
                    try {
                        if (shutDownRequested) {
                            log.info("Shutdown requested, exiting !");
                            break;
                        }
                        populateEnv(env, conn);
                        List<Deployment> deployments = getDeployments(conn, env);
                        executePlugins(env, organizationsMapCache, deployments);
                        updateCrawlEntry(env);
                    } catch (Exception e) {
                        log.error("Error while processing env, will skip and continue. env id " + env.getId(), e);
                    }
                }

                long endTimeMillis = System.currentTimeMillis();
                log.info("Time taken to crawl all environments and execute all plugins in seconds: " + (endTimeMillis - startTimeMillis)/(1000));
                platformHADRCrawlerPlugin.cleanup();
                if (this.singleRun) {
                    log.info("Crawler is configured to exit after single run");
                    System.exit(0);
                }

                log.info("crawled all environments, will go over again.");

                if (syncClouds) {
                    crawlClouds(conn);
                }
                Thread.sleep(crawlFrequencyHours * 60 * 60 * 1000);//sleep before next crawl
            }
        } catch (Throwable e) {
            log.error("Error, Crawler will stop : ", e);
        }
    }

    private void executePlugins(Environment env, Map<String, Organization> organizationsMapCache, List<Deployment> deployments) {
        ttlPlugin.processEnvironment(env, deployments, organizationsMapCache);
        platformHADRCrawlerPlugin.processEnvironment(env, organizationsMapCache);

        if (scaleDownPlugin.isEnabled()) {
            scaleDownPlugin.processEnvironment(env, organizationsMapCache);
        } else {
            log.info(scaleDownPlugin.getPluginName() + " plugin not enabled");
        }
    }

    private void crawlClouds(Connection conn) {

        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

        log.info("Fetching all clouds..");
        Result<Record> cloudRecords = create.select().from(CM_CI)
                .join(MD_CLASSES).on(CM_CI.CLASS_ID.eq(MD_CLASSES.CLASS_ID))
                .join(NS_NAMESPACES).on(CM_CI.NS_ID.eq(NS_NAMESPACES.NS_ID))
                .where(MD_CLASSES.CLASS_NAME.eq("account.Cloud"))
                .fetch(); //all the env cis
        log.info("Got all clouds ");


        for (Record r : cloudRecords) {
            if (shutDownRequested) {
                log.info("Shutdown requested, exiting !");
                break;
            }

            long cloudId = r.getValue(CM_CI.CI_ID);
            String cloudName = r.getValue(CM_CI.CI_NAME);
            String cloudNS = r.getValue(NS_NAMESPACES.NS_PATH);
            if (syncClouds) {
                syncCloudVariables(cloudId, cloudName, cloudNS, conn);
            }
        }
    }

    private void syncCloudVariables(long cloudId, String cloudName, String cloudNS, Connection conn) {
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        Integer valueForRelationId = create.select().from(MD_RELATIONS)
                .where(MD_RELATIONS.RELATION_NAME.eq("account.ValueFor"))
                .fetchAny().getValue(MD_RELATIONS.RELATION_ID);

        int varClassId = create.select().from(MD_CLASSES)
                .where(MD_CLASSES.CLASS_NAME.eq("account.Cloudvar"))
                .fetchAny().getValue(MD_CLASSES.CLASS_ID);

        int valueAttributeId = create.select().from(MD_CLASSES)
                .join(MD_CLASS_ATTRIBUTES).on(MD_CLASSES.CLASS_ID.eq(MD_CLASS_ATTRIBUTES.CLASS_ID))
                .where(MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME.eq("value")
                        .and(MD_CLASSES.CLASS_NAME.eq("base.Cloudvar")))
                .fetchAny().getValue(MD_CLASS_ATTRIBUTES.ATTRIBUTE_ID);

        int secureAttributeId = create.select().from(MD_CLASSES).join(MD_CLASS_ATTRIBUTES).on(MD_CLASSES.CLASS_ID.eq(MD_CLASS_ATTRIBUTES.CLASS_ID))
                .where(MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME.eq("secure")
                        .and(MD_CLASSES.CLASS_NAME.eq("base.Cloudvar")))
                .fetchAny().getValue(MD_CLASS_ATTRIBUTES.ATTRIBUTE_ID);

        String varNS = cloudNS + "/" + cloudName;

        String value = "M";

        if (cloudName.toLowerCase().contains("prod")
                || cloudName.toLowerCase().contains("azure")) {
            value = "M";
        }

        long varNsId = create.select().from(NS_NAMESPACES)
                .where(NS_NAMESPACES.NS_PATH.eq(varNS))
                .fetchAny().getValue(NS_NAMESPACES.NS_ID);

        //insert variable ci
        long nextID = create.nextval(Sequences.CM_PK_SEQ);
        long newVarId = nextID;
        create.insertInto(CM_CI, CM_CI.CI_ID, CM_CI.NS_ID, CM_CI.CI_NAME, CM_CI.CLASS_ID, CM_CI.CI_GOID, CM_CI.CI_STATE_ID)
                .values(nextID, varNsId, "size", varClassId, varNsId + "-" + varClassId + "-" + nextID, 100).returning(CM_CI.CI_ID)
                .fetchOne().getValue(CM_CI.CI_ID);

        nextID = create.nextval(Sequences.CM_PK_SEQ);
        //insert attributes
        create.insertInto(CM_CI_ATTRIBUTES, CM_CI_ATTRIBUTES.CI_ATTRIBUTE_ID, CM_CI_ATTRIBUTES.CI_ID, CM_CI_ATTRIBUTES.ATTRIBUTE_ID, CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE, CM_CI_ATTRIBUTES.DJ_ATTRIBUTE_VALUE)
                .values(nextID, newVarId, valueAttributeId, value, value).execute();

        nextID = create.nextval(Sequences.CM_PK_SEQ);
        create.insertInto(CM_CI_ATTRIBUTES, CM_CI_ATTRIBUTES.CI_ATTRIBUTE_ID, CM_CI_ATTRIBUTES.CI_ID, CM_CI_ATTRIBUTES.ATTRIBUTE_ID, CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE, CM_CI_ATTRIBUTES.DJ_ATTRIBUTE_VALUE)
                .values(nextID, newVarId, secureAttributeId, "false", "false").execute();

        String relationGoId = newVarId + "-" + valueForRelationId + "-" + cloudId;
        nextID = create.nextval(Sequences.CM_PK_SEQ);
        create.insertInto(CM_CI_RELATIONS, CM_CI_RELATIONS.CI_RELATION_ID, CM_CI_RELATIONS.NS_ID, CM_CI_RELATIONS.RELATION_ID,
                CM_CI_RELATIONS.FROM_CI_ID, CM_CI_RELATIONS.TO_CI_ID, CM_CI_RELATIONS.RELATION_GOID, CM_CI_RELATIONS.CI_STATE_ID)
                .values(nextID, varNsId, valueForRelationId, newVarId, cloudId, relationGoId, 100).execute();

    }

    private void init(Connection conn) {
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> coresAttributes = create.select().from(MD_CLASS_ATTRIBUTES)
        .join(MD_CLASSES).on(MD_CLASS_ATTRIBUTES.CLASS_ID.eq(MD_CLASSES.CLASS_ID))
        .where(MD_CLASSES.CLASS_NAME.like("bom%Compute"))
        .and(MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME.eq("cores")).fetch();

        for (Record coresAttribute : coresAttributes) {
            coresAttributeIds.add(coresAttribute.getValue(MD_CLASS_ATTRIBUTES.ATTRIBUTE_ID));
        }

        //create = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> computeClasses = create.select().from(MD_CLASSES)
                .where(MD_CLASSES.CLASS_NAME.like("bom%Compute")).fetch();
        for (Record computeClass : computeClasses) {
            computeClassIds.add(computeClass.get(MD_CLASSES.CLASS_ID));
        }
        log.info("cached compute class ids: " + computeClassIds);
        log.info("cached compute cores attribute ids: " + coresAttributeIds);
        populateBaseOrganizationClassAttribMappingsCache(conn);

        //cache relation attribute ids
        Result<Record> relationsAttributes = create.select().from(MD_RELATION_ATTRIBUTES).join(MD_RELATIONS)
                .on(MD_RELATION_ATTRIBUTES.RELATION_ID.eq(MD_RELATIONS.RELATION_ID))
                .fetch();
        for (Record relationAttribute : relationsAttributes) {
            if (relationAttribute.getValue(MD_RELATIONS.RELATION_NAME)
                    .equalsIgnoreCase("manifest.ComposedOf")
                    && relationAttribute.getValue(MD_RELATION_ATTRIBUTES.ATTRIBUTE_NAME).equalsIgnoreCase("enabled")) {
                //cache the "enabled" attribute id of platform
                platformEnabledAttributeId = relationAttribute.getValue(MD_RELATION_ATTRIBUTES.ATTRIBUTE_ID);
            }
            //Here cache more attribute ids as needed for different cases
        }
    }

    private void populateEnv(Environment env, Connection conn) {
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> envAttributes = create.select().from(CM_CI_ATTRIBUTES)
                .join(MD_CLASS_ATTRIBUTES).on(CM_CI_ATTRIBUTES.ATTRIBUTE_ID.eq(MD_CLASS_ATTRIBUTES.ATTRIBUTE_ID))
                .where(CM_CI_ATTRIBUTES.CI_ID.eq(env.getId()))
                .fetch();
        for (Record attrib : envAttributes) {
            String attributeName = attrib.getValue(MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME);
            if (attributeName.equalsIgnoreCase("profile")) {
                env.setProfile(attrib.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));
            }
            //add other attributes as and when needed
        }
        //now query all the platforms for this env
        Result<Record> platformRels = create.select().from(CM_CI_RELATIONS)
                .join(MD_RELATIONS).on(MD_RELATIONS.RELATION_ID.eq(CM_CI_RELATIONS.RELATION_ID))
                .join(CM_CI).on(CM_CI.CI_ID.eq(CM_CI_RELATIONS.TO_CI_ID))
                .where(MD_RELATIONS.RELATION_NAME.eq("manifest.ComposedOf"))
                .and(CM_CI_RELATIONS.FROM_CI_ID.eq(env.getId()))
                .fetch();
        int totalCores = 0;
        for (Record platformRel : platformRels) {
            long platformId = platformRel.getValue(CM_CI_RELATIONS.TO_CI_ID);
            Platform platform = new Platform();
            platform.setId(platformId);
            platform.setName(platformRel.getValue(CM_CI.CI_NAME));
            platform.setPath(env.getPath() + "/" + env.getName() + "/bom/" + platform.getName() + "/1");
            populatePlatform(conn, platform, platformRel.getValue(CM_CI_RELATIONS.CI_RELATION_ID));
            platform.setActiveClouds(getActiveClouds(platform, conn));
            platform.setCloudsMap(getCloudsDataForPlatform(conn, platformId));
            
            //now calculate total cores of the env - including all platforms
            totalCores += platform.getTotalCores();
            env.addPlatform(platform);
        }
        env.setTotalCores(totalCores);
    }

    private List<Deployment> getDeployments(Connection conn, Environment env) {

        List<Deployment> deployments = new ArrayList<>();
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> records = create.select().from(DJ_DEPLOYMENT)
                .join(DJ_DEPLOYMENT_STATES).on(DJ_DEPLOYMENT_STATES.STATE_ID.eq(DJ_DEPLOYMENT.STATE_ID))
                .join(NS_NAMESPACES).on(NS_NAMESPACES.NS_ID.eq(DJ_DEPLOYMENT.NS_ID))
                .where(NS_NAMESPACES.NS_PATH.eq(env.getPath()+ "/" + env.getName() + "/bom"))
                .and(DJ_DEPLOYMENT.CREATED_BY.notEqual("oneops-autoreplace"))
                .orderBy(DJ_DEPLOYMENT.CREATED.desc())
                .limit(10)
                .fetch();
        for (Record r : records) {
            Deployment deployment = new Deployment();
            deployment.setCreatedAt(r.getValue(DJ_DEPLOYMENT.CREATED));
            deployment.setCreatedBy(r.getValue(DJ_DEPLOYMENT.CREATED_BY));
            deployment.setState(r.getValue(DJ_DEPLOYMENT_STATES.STATE_NAME));
            deployment.setDeploymentId(r.getValue(DJ_DEPLOYMENT.DEPLOYMENT_ID));
            deployments.add(deployment);
        }
        return deployments;
    }

    private List<Environment> getOneopsEnvironments(Connection conn) {
        List<Environment> envs = new ArrayList<>();
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        log.info("Fetching all environments..");
        Result<Record> envRecords = create.select().from(CM_CI)
                .join(MD_CLASSES).on(CM_CI.CLASS_ID.eq(MD_CLASSES.CLASS_ID))
                .join(NS_NAMESPACES).on(CM_CI.NS_ID.eq(NS_NAMESPACES.NS_ID))
                .where(MD_CLASSES.CLASS_NAME.eq("manifest.Environment"))
                .fetch(); //all the env cis
        log.info("Got all environments");
        for (Record r : envRecords) {
            long envId = r.getValue(CM_CI.CI_ID);
            //now query attributes for this env
            Environment env = new Environment();
            env.setName(r.getValue(CM_CI.CI_NAME));
            env.setId(r.getValue(CM_CI.CI_ID));
            env.setPath(r.getValue(NS_NAMESPACES.NS_PATH));
            env.setNsId(r.getValue(NS_NAMESPACES.NS_ID));
            envs.add(env);
        }
        return envs;
    }

    private List<String> getActiveClouds(Platform platform, Connection conn) {
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        List<String> clouds = new ArrayList<>();
        Result<Record> consumesRecords = create.select().from(CM_CI_RELATIONS)
                .join(MD_RELATIONS).on(MD_RELATIONS.RELATION_ID.eq(CM_CI_RELATIONS.RELATION_ID))
                .join(CM_CI_RELATION_ATTRIBUTES).on(CM_CI_RELATION_ATTRIBUTES.CI_RELATION_ID.eq(CM_CI_RELATIONS.CI_RELATION_ID))
                .where(CM_CI_RELATIONS.FROM_CI_ID.eq(platform.getId()))
                .and(CM_CI_RELATION_ATTRIBUTES.DF_ATTRIBUTE_VALUE.eq("active"))
                .fetch();
        for (Record r : consumesRecords) {
            String comments = r.getValue(CM_CI_RELATIONS.COMMENTS);
            String cloudName = comments.split(":")[1];
            cloudName = cloudName.split("\"")[1];
            clouds.add(cloudName);
        }
        return clouds;
    }

    private void populatePlatform(Connection conn, Platform platform, long composedOfRelationId) {
        DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> computes = create.select().from(CM_CI)
                .join(NS_NAMESPACES).on(NS_NAMESPACES.NS_ID.eq(CM_CI.NS_ID))
                .join(CM_CI_ATTRIBUTES).on(CM_CI_ATTRIBUTES.CI_ID.eq(CM_CI.CI_ID))
                .where(NS_NAMESPACES.NS_PATH.eq(platform.getPath())
                .and(CM_CI.CLASS_ID.in(computeClassIds))
                .and(CM_CI_ATTRIBUTES.ATTRIBUTE_ID.in(coresAttributeIds)))
                .fetch();

        platform.setTotalComputes(computes.size());
        int totalCores = 0;
        if (platform.getTotalComputes() > 0) {
            for (Record compute : computes) {
                totalCores += Integer.parseInt(compute.get(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));
            }
        }
        platform.setTotalCores(totalCores);

        //Now query platform ci attributes and set to the object
        Result<Record> platformAttributes = create.select().from(CM_CI_ATTRIBUTES)
                .join(MD_CLASS_ATTRIBUTES).on(MD_CLASS_ATTRIBUTES.ATTRIBUTE_ID.eq(CM_CI_ATTRIBUTES.ATTRIBUTE_ID))
                .where(CM_CI_ATTRIBUTES.CI_ID.eq(platform.getId()))
                .fetch();

        for (Record attribute : platformAttributes) {
            String attributeName = attribute.getValue(MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME);
            if (attributeName.equalsIgnoreCase("source")) {
                platform.setSource(attribute.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));
            } else if (attributeName.equalsIgnoreCase("pack")) {
                platform.setPack(attribute.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));
            } else if (attributeName.equalsIgnoreCase("autorepair")) {
              platform.setAutoRepairEnabled(new Boolean(attribute.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE)));
            } else if (attributeName.equalsIgnoreCase("autoreplace")) {
                platform.setAutoReplaceEnabled(new Boolean(attribute.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE)));
            }
        }
        //Now set the enable/disable status
        //select * from cm_ci_relation_attributes where ci_relation_id=composedOfRelationId
        Result<Record> composedOfRelationAttributes = create.select().from(CM_CI_RELATION_ATTRIBUTES)
                .where(CM_CI_RELATION_ATTRIBUTES.CI_RELATION_ID.eq(composedOfRelationId))
                .fetch();
        for (Record relAttribute : composedOfRelationAttributes) {
            if (relAttribute.getValue(CM_CI_RELATION_ATTRIBUTES.ATTRIBUTE_ID) == platformEnabledAttributeId) {
                boolean enabled = Boolean.valueOf(relAttribute.getValue(CM_CI_RELATION_ATTRIBUTES.DF_ATTRIBUTE_VALUE));
                if (enabled) {
                    platform.setEnable("enable");//this could have been a boolean, but model has dependency on nubu
                } else {
                    platform.setEnable("disable");
                }
            }
        }
    }

    private void updateCrawlEntry(Environment env) {
        if (crawlerDbUserName == null || crawlerDbUrl == null || crawlerDbPassword == null) {
            return;
        }
        try (Connection conn = DriverManager.getConnection(crawlerDbUrl, crawlerDbUserName, crawlerDbPassword)) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            Result<Record> records = create.select().from(CRAWL_ENTITIES)
                    .where(CRAWL_ENTITIES.OO_ID.eq(env.getId()))
                    .fetch();

            if (records.isNotEmpty()) {
                create.update(CRAWL_ENTITIES)
                        .set(CRAWL_ENTITIES.LAST_CRAWLED_AT, new Timestamp(System.currentTimeMillis()))
                        .where(CRAWL_ENTITIES.OO_ID.eq(env.getId()))
                        .execute();
            } else {
                create.insertInto(CRAWL_ENTITIES)
                        .set(CRAWL_ENTITIES.NS_PATH, env.getPath() + "/" + env.getName())
                        .set(CRAWL_ENTITIES.OO_ID, env.getId())
                        .execute();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] a) {
        new CMSCrawler().crawl();
    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            log.info("Shutdown requested");
            shutDownRequested = true;
        }
    }
    
  public Map<String, Organization> populateOrganizations(Connection conn) {

    log.info("Populating organizations cache");
    DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
    Map<String, Organization> organizationsMap = new HashMap<>();

    Result<Record4<Long, String, Integer, String>> OrganizationsWithAttributesRecords = create
        .select(CM_CI.CI_ID, CM_CI.CI_NAME, CM_CI_ATTRIBUTES.ATTRIBUTE_ID,
            CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE)
        .from(CM_CI).join(CM_CI_ATTRIBUTES).on(CM_CI.CI_ID.eq(CM_CI_ATTRIBUTES.CI_ID))
        .where(CM_CI.CLASS_ID.in(create.select(MD_CLASSES.CLASS_ID).from(MD_CLASSES)
            .where(MD_CLASSES.CLASS_NAME.eq("account.Organization"))))
        .fetch();

    List<Long> OrganizationIds = OrganizationsWithAttributesRecords.getValues(CM_CI.CI_ID);
    log.debug("OrganizationIds: " + OrganizationIds.toString());

    Set<Long> setOfOrganizationIds = new HashSet<Long>(OrganizationIds);
    log.debug("setOfOrganizationIds <" + setOfOrganizationIds.size() + "> " + setOfOrganizationIds);

    List<String> OrganizationNames = OrganizationsWithAttributesRecords.getValues(CM_CI.CI_NAME);
    log.debug("OrganizationNames: " + OrganizationNames.toString());

    Set<String> setOfOrganizationNames = new HashSet<String>(OrganizationNames);
    log.debug("setOfOrganizationNames: <" + setOfOrganizationNames.size() + "> "
        + setOfOrganizationNames);

    int description_AttribID =
        this.baseOrganizationMDClassAttributes_NameIdMapCache.get("description");
    int full_name_AttribID = this.baseOrganizationMDClassAttributes_NameIdMapCache.get("full_name");
    int owner_AttribID = this.baseOrganizationMDClassAttributes_NameIdMapCache.get("owner");
    int tags_AttribID = this.baseOrganizationMDClassAttributes_NameIdMapCache.get("tags");

    for (Record4<Long, String, Integer, String> OrganizationsWithAttributesRecord : OrganizationsWithAttributesRecords) {
      long organizationId = OrganizationsWithAttributesRecord.getValue(CM_CI.CI_ID);

      String organizationName = OrganizationsWithAttributesRecord.getValue(CM_CI.CI_NAME);
      Organization organization = organizationsMap.get(organizationName);
      log.debug("organizationId: " + organizationId);
      if (organization == null) {
        organization = new Organization();
        organizationsMap.put(organizationName, organization);

      }

      int attributeID = OrganizationsWithAttributesRecord.getValue(CM_CI_ATTRIBUTES.ATTRIBUTE_ID);

      if (attributeID == description_AttribID) {
        organization.setDescription(
            OrganizationsWithAttributesRecord.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));

        continue;
      } else if (attributeID == full_name_AttribID) {
        organization.setFull_name(
            OrganizationsWithAttributesRecord.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));

        continue;

      } else if (attributeID == owner_AttribID) {
        organization.setOwner(
            OrganizationsWithAttributesRecord.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));

        continue;
      } else if (attributeID == tags_AttribID) {
        @SuppressWarnings("unchecked") 
        Map<String, String> tags = gson.fromJson(
            OrganizationsWithAttributesRecord.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE),
            Map.class);
        organization.setTags(tags);
        
        continue;
      }


    }

    log.info("Caching for Org Data Complete");
    return organizationsMap;
  }
  
  private void populateBaseOrganizationClassAttribMappingsCache(Connection conn) {
    DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);
    log.debug("populating Organization Class Attribute Mappings Cache");

    this.baseOrganizationMDClassAttributes_NameIdMapCache =
        create.select(MD_CLASS_ATTRIBUTES.ATTRIBUTE_ID, MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME)
            .from(MD_CLASS_ATTRIBUTES).join(MD_CLASSES)
            .on(MD_CLASS_ATTRIBUTES.CLASS_ID.eq(MD_CLASSES.CLASS_ID))
            .where(MD_CLASSES.CLASS_NAME.eq("base.Organization")).fetch()
            .intoMap(MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME, MD_CLASS_ATTRIBUTES.ATTRIBUTE_ID);

    log.debug("baseOrganizationMDClassAttributes_NameIdMapCache: entrySet"
        + this.baseOrganizationMDClassAttributes_NameIdMapCache.entrySet());

  }
	
  public Map<String, Cloud> getCloudsDataForPlatform(Connection conn, long platformId) {
    DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

    Map<String, Cloud> platformCloudMap = new HashMap<String, Cloud>();

    // Fetching All Clouds for platform
    Result<Record> cloudsInPlatformRecords = create.select().from(CM_CI_RELATIONS)
        .join(MD_RELATIONS).on(MD_RELATIONS.RELATION_ID.eq(CM_CI_RELATIONS.RELATION_ID)).join(CM_CI)
        .on(CM_CI.CI_ID.eq(CM_CI_RELATIONS.TO_CI_ID))
        .where(MD_RELATIONS.RELATION_NAME.eq("base.Consumes"))
        .and(CM_CI_RELATIONS.FROM_CI_ID.eq(platformId)).fetch();

    for (Record cloudsInPlatformRecord : cloudsInPlatformRecords) {

      long relationID = cloudsInPlatformRecord.get(CM_CI_RELATIONS.CI_RELATION_ID);
      long cloudCid = cloudsInPlatformRecord.get(CM_CI_RELATIONS.TO_CI_ID);
      String cloudName = cloudsInPlatformRecord.get(CM_CI.CI_NAME);

      Result<Record> cloudsPlatformRelationshipAttributesRecords =
          create.select().from(CM_CI_RELATION_ATTRIBUTES).join(MD_RELATION_ATTRIBUTES)
              .on(CM_CI_RELATION_ATTRIBUTES.ATTRIBUTE_ID.eq(MD_RELATION_ATTRIBUTES.ATTRIBUTE_ID))
              .where(CM_CI_RELATION_ATTRIBUTES.CI_RELATION_ID.eq(relationID)).fetch();
      Cloud cloud = new Cloud();
      cloud.setId(cloudName);
      for (Record cloudsPlatformRelationshipAttributesRecord : cloudsPlatformRelationshipAttributesRecords) {

          String attributeValue = cloudsPlatformRelationshipAttributesRecord
                .get(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE);

        switch (cloudsPlatformRelationshipAttributesRecord
            .get(MD_RELATION_ATTRIBUTES.ATTRIBUTE_NAME)) {
          case "priority":
              if (NumberUtils.isNumber(attributeValue)) {
                  int priority = Integer.valueOf(attributeValue.trim());
                  cloud.setPriority(priority);
              } else {
                  log.warn("can not set priority attribute for cloudCid: " + cloudCid
                          + " , cloudName: " + cloudName + " attributeValue: " + attributeValue);
              }

            break;
          case "adminstatus":
            cloud.setAdminstatus(attributeValue);
            break;
          case "dpmt_order":
            if (NumberUtils.isNumber(attributeValue)) {
                cloud.setDeploymentorder(Integer.valueOf(attributeValue.trim()));
            } else {
                log.warn("can not set dpmt order attribute for cloudCid: " + cloudCid
                        + " , cloudName: " + cloudName + " attributeValue: " + attributeValue);
            }
            break;
          case "pct_scale":
              if (NumberUtils.isNumber(attributeValue)) {
                  cloud.setScalepercentage(Integer.valueOf(attributeValue.trim()));
              } else {
                  log.warn("can not set pct_scale attribute for cloudCid: " + cloudCid
                          + " , cloudName: " + " attributeValue: " + attributeValue);
              }
            break;
        }
      }
      platformCloudMap.put(cloudName, cloud);


    }

    return platformCloudMap;

  }
}
