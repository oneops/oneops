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

import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Platform;
import com.oneops.crawler.jooq.cms.Sequences;
import com.oneops.crawler.plugins.ttl.EnvTTLCrawlerPlugin;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    boolean fullSweepAtStart = true;
    boolean shutDownRequested = false;
    boolean syncClouds = false;
    int crawlFrequencyHours = 6;

    public CMSCrawler() {
        //read and init the secrets
        readConfig();
    }

    private void readConfig() {
        String secretsPropertiesFilePath = System.getProperty("secrets.properties.file",
                "/secrets/crawler_secrets.properties");
        File secretsFile = new File(secretsPropertiesFilePath);

        if (secretsFile.exists()) {
            try {
                readSecrets(secretsFile);
            } catch (IOException e) {
                readSystemProperties();
            }
        } else {
            readSystemProperties();
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

        String frequencyProperty = System.getProperty("crawler.freqency.hrs");
        if (frequencyProperty != null) {
            crawlFrequencyHours = Integer.parseInt(frequencyProperty);
        }
        //log.info("crawlerDbUserName: " + crawlerDbUserName + "; crawlerDbPassword: " + crawlerDbPassword + " crawlerDbUrl: " + crawlerDbUrl);
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
    }

    public void crawl() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        try (Connection conn = DriverManager.getConnection(cmsDbUrl, cmsDbUserName, cmsDbPassword)) {
            init(conn);
            List<Environment> envs = getOneopsEnvironments(conn);
            EnvTTLCrawlerPlugin plugin = new EnvTTLCrawlerPlugin(); //TODO: auto-discover plugins from jars
            long envsLastFetchedAt = System.currentTimeMillis();

            while (true && !shutDownRequested) {
                if ((System.currentTimeMillis() - envsLastFetchedAt)/(1000 * 60 * 60 * 24) >= 1 ) { //been a day
                    envs = getOneopsEnvironments(conn);//refresh the environment list
                }
                log.info("Starting to crawl all environments.. Total # " + envs.size());
                for (Environment env : envs) {
                    if (shutDownRequested) {
                        log.info("Shutdown requested, exiting !");
                        break;
                    }
                    populateEnv(env, conn);
                    List<Deployment> deployments = getDeployments(conn, env);
                    plugin.processEnvironment(env, deployments);
                    updateCrawlEntry(env);
                }

                log.info("crawled all environments, will go over again.");

                if (syncClouds) {
                    crawlClouds(conn);
                }

                Thread.sleep(crawlFrequencyHours * 60 * 60 * 1000);//sleep before next crawl
            }
        } catch (Throwable e) {
            e.printStackTrace();
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

        create = DSL.using(conn, SQLDialect.POSTGRES);
        Result<Record> computeClasses = create.select().from(MD_CLASSES)
                .where(MD_CLASSES.CLASS_NAME.like("bom%Compute")).fetch();
        for (Record computeClass : computeClasses) {
            computeClassIds.add(computeClass.get(MD_CLASSES.CLASS_ID));
        }
        log.info("cached compute class ids: " + computeClassIds);
        log.info("cached compute cores attribute ids: " + coresAttributeIds);
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
            populatePlatform(conn, platform);
            platform.setActiveClouds(getActiveClouds(platform, conn));

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
                .limit(1)
                .fetch();
        for (Record r : records) {
            Deployment deployment = new Deployment();
            deployment.setCreatedAt(r.getValue(DJ_DEPLOYMENT.CREATED));
            deployment.setCreatedBy(r.getValue(DJ_DEPLOYMENT.CREATED_BY));
            deployment.setState(r.getValue(DJ_DEPLOYMENT_STATES.STATE_NAME));
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

    private void populatePlatform(Connection conn, Platform platform) {
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

        for ( Record attribute : platformAttributes ) {
            String attributeName = attribute.getValue(MD_CLASS_ATTRIBUTES.ATTRIBUTE_NAME);
            if (attributeName.equalsIgnoreCase("source")) {
                platform.setSource(attribute.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));
            } else if (attributeName.equalsIgnoreCase("pack")) {
                platform.setPack(attribute.getValue(CM_CI_ATTRIBUTES.DF_ATTRIBUTE_VALUE));
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
}
