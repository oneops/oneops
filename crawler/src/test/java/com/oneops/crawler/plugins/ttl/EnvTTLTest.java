package com.oneops.crawler.plugins.ttl;

import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Organization;
import com.oneops.Platform;
import com.oneops.crawler.ESRecord;
import com.oneops.crawler.OneOpsFacade;
import com.oneops.crawler.SearchDal;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class EnvTTLTest {

    EnvTTLCrawlerPlugin ttlPlugin = null;
    OneOpsFacade ooFacade = null;
    SearchDal searchDal = null;
    Environment env = null;
    ArrayList<Deployment> deployments = null;
    Platform platform_2 = null;
    Platform platform_1 = null;
    Deployment deployment = null;
    Date today = new Date(System.currentTimeMillis());
    StringBuilder esQuery = new StringBuilder();

    @BeforeClass
    public void setUp() throws Exception {
        //create environment mock
        env = new Environment();
        env.setProfile("QA");
        env.setPath("/testOrg/testAssembly");
        env.setName("testEnv");
        env.setId(1);

        //create platform mocks and set them to above env
        platform_1 = new Platform();
        platform_1.setName("platform_1");
        platform_1.setId(101);
        platform_1.setPath("/testOrg/testAssembly");
        platform_1.setTotalComputes(10);
        platform_1.setTotalCores(40);
        platform_1.setEnable("enable");

        ArrayList<String> clouds = new ArrayList<>();
        clouds.add("cdc1");
        clouds.add("ctf3");
        platform_1.setActiveClouds(clouds);

        platform_2 = new Platform();
        platform_2.setName("platform_2");
        platform_2.setId(102);
        platform_2.setPath("/testOrg/testAssembly");
        platform_2.setTotalComputes(100);
        platform_2.setTotalCores(400);
        platform_2.setEnable("enable");

        clouds = new ArrayList<>();
        clouds.add("testCloud");
        clouds.add("devCloud");
        platform_2.setActiveClouds(clouds);

        env.addPlatform(platform_2);
        env.addPlatform(platform_1);

        //create deployment
        deployment = new Deployment();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -70);//older than 2 months
        deployment.setCreatedAt(cal.getTime());
        deployment.setCreatedBy("user_1");
        deployment.setState("complete");

        deployments = new ArrayList<>();
        deployments.add(deployment);

        ttlPlugin = new EnvTTLCrawlerPlugin();

        ooFacade = Mockito.mock(OneOpsFacade.class);
        searchDal = Mockito.mock(SearchDal.class);

        ttlPlugin.setOoFacade(ooFacade);
        ttlPlugin.setSearchDal(searchDal);
        ttlPlugin.setTtlBotName("OneOps-TTL-Bot");
        ttlPlugin.init();
        ttlPlugin.setTtlEnabled(true);
        ttlPlugin.setGracePeriodDays(0);
        Path path = Paths.get(getClass().getClassLoader().getResource("activeTtlRecordQuery.json").toURI());
        Stream<String> lines;
        lines = Files.lines(path);
        lines.forEach(line -> esQuery.append(line).append(System.lineSeparator()));
    }

    @Test
    public void testFirstScan() throws Exception {

        //make sure it does not get ttled right on first scan
        ttlPlugin.processEnvironment(env, deployments, new HashMap<>());
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

    }

    @Test
    public void testPastDueTTL() throws Exception {
        //set the destroyDate as past-due and make sure it gets ttled on second scan
        // because the grace period is set to 0 days for this test
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2); //set planned destroy date as past-due
        Date destroyDate = calendar.getTime();
        EnvironmentTTLRecord ttlRecord = new EnvironmentTTLRecord();
        ttlRecord.setPlannedDestroyDate(destroyDate);
        ttlRecord.setUserNotifiedTimes(2);
        ESRecord esRecord = new ESRecord();
        esRecord.setSource(ttlRecord);

        List<ESRecord> ttlRecordList = new ArrayList<>();

        ttlRecordList.add(esRecord);
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -150);//last deployment 150 days back
        deployment.setCreatedAt(cal.getTime());
        deployment.setState("complete");
        deployments = new ArrayList<>();
        deployments.add(deployment);

        String platform_1EsQuery = esQuery.toString()
                .replace("<platformId>", "" + platform_1.getId())
                .replace("<ttlDeploymentSubmitted>", String.valueOf(false));
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_1EsQuery)))
                .thenReturn(ttlRecordList);

        String platform_2EsQuery = esQuery.toString()
                .replace("<platformId>", "" + platform_2.getId())
                .replace("<ttlDeploymentSubmitted>", String.valueOf(false));
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_2EsQuery)))
                .thenReturn(ttlRecordList);

        env.setProfile("QA");
        ttlPlugin.processEnvironment(env, deployments, new HashMap<>());

        Mockito.verify(ooFacade, Mockito.times(1)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        //second platform should not be disabled in the same run
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(1)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        //second platform should not be deployed in the same run
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

        Mockito.reset(ooFacade);
        //now simulate the platform_1 is ttled already
        platform_1.setTotalComputes(0);
        ttlPlugin.processEnvironment(env, deployments, new HashMap<>());
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(1)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(1)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

    }

    @Test
    public void testActivelyDeployedEnv() throws Exception {
        //test that recently deployed env is NOT ttled
        Calendar cal = Calendar.getInstance();
        Date today = new Date(System.currentTimeMillis());

        cal.setTime(today);
        cal.add(Calendar.DATE, -10);//10 days back
        Deployment deployment = new Deployment();
        deployment.setCreatedBy("user_1");
        deployment.setCreatedAt(cal.getTime());
        deployments = new ArrayList<>();
        deployments.add(deployment);
        ooFacade = Mockito.mock(OneOpsFacade.class);
        ttlPlugin.processEnvironment(env, deployments, new HashMap<>());
        ttlPlugin.setOoFacade(ooFacade);

        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);
    }

    @Test
    public void testProdEnv() throws Exception {
        //test that prod env is NOT ttled
        Calendar cal = Calendar.getInstance();
        Date today = new Date(System.currentTimeMillis());
        Deployment deployment = new Deployment();
        deployment.setCreatedBy("user_1");
        cal.setTime(today);
        cal.add(Calendar.DATE, -200);//no deployment since 200 days
        deployment.setCreatedAt(cal.getTime());
        deployments = new ArrayList<>();
        deployments.add(deployment);
        env.setProfile("prod");
        ooFacade = Mockito.mock(OneOpsFacade.class);
        ttlPlugin.processEnvironment(env, deployments, new HashMap<>());
        ttlPlugin.setOoFacade(ooFacade);

        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);
    }


    @Test
    public void testTtlPostProcessing() throws Exception {
        //set the destroyDate as past-due and make sure it gets ttled on second scan
        // because the grace period is set to 0 days for this test
        EnvironmentTTLRecord ttlRecord1 = new EnvironmentTTLRecord();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2); //set planned destroy date as past-due
        Date destroyDate = calendar.getTime();
        ttlRecord1.setPlannedDestroyDate(destroyDate);
        ttlRecord1.setUserNotifiedTimes(1);
        ttlRecord1.setPlatform(platform_1);

        EnvironmentTTLRecord ttlRecord2 = new EnvironmentTTLRecord();
        ttlRecord2.setPlannedDestroyDate(destroyDate);
        ttlRecord2.setUserNotifiedTimes(1);
        ttlRecord2.setPlatform(platform_2);

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -150);//last deployment 150 days back
        deployment.setCreatedAt(cal.getTime());
        deployments = new ArrayList<>();
        deployments.add(deployment);
        deployment.setState("complete");

        env.setProfile("QA");
        ttlPlugin.setEsEnabled(true);
        //SearchDal searchDal = new SearchDal();
        //searchDal.setEsHost("searchdb");
        ttlPlugin.setSearchDal(searchDal);
        ttlPlugin.createIndex();

        Map<String, Organization> orgs = new HashMap<>();
        Organization testOrg = new Organization();
        Map<String, String> tags = new HashMap<>();
        testOrg.setTags(tags);
        tags.put("VP", "first lastname");
        orgs.put("testOrg", testOrg);

        platform_1.setTotalComputes(10);
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.anyObject()))
                .thenReturn(null);

        ttlPlugin.processEnvironment(env, deployments, orgs);

        //1. verify that disable/deploy apis not called and searchDal is called with ttlrecord.deploymentSubmitted=false
        Mockito.verify(searchDal, Mockito.times(1)).post(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Matchers.argThat(new TtlDeploymentSubmitted(false, 1, 0, 0, platform_1)));
        Mockito.verify(searchDal, Mockito.times(1)).post(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Matchers.argThat(new TtlDeploymentSubmitted(false, 1, 0, 0, platform_2)));

        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

        //1.end

        //2. on second time, make sure that the userNotified is updated and still the platform is not disabled

        ESRecord esRecord1 = new ESRecord();
        esRecord1.setSource(ttlRecord1);
        esRecord1.setId("1.1");
        List<ESRecord> ttlRecordList1 = new ArrayList<>();
        ttlRecordList1.add(esRecord1);

        ESRecord esRecord2 = new ESRecord();
        esRecord2.setSource(ttlRecord2);
        esRecord2.setId("1.2");
        List<ESRecord> ttlRecordList2 = new ArrayList<>();
        ttlRecordList2.add(esRecord2);


        String platform_1EsQuery = esQuery.toString()
                .replace("<platformId>", "" + platform_1.getId())
                .replace("<ttlDeploymentSubmitted>", String.valueOf(false));
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_1EsQuery)))
                .thenReturn(ttlRecordList1);

        String platform_2EsQuery = esQuery.toString()
                .replace("<platformId>", "" + platform_2.getId())
                .replace("<ttlDeploymentSubmitted>", String.valueOf(false));
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_2EsQuery)))
                .thenReturn(ttlRecordList2);

        ttlPlugin.processEnvironment(env, deployments, orgs);

        Mockito.verify(searchDal).put(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Matchers.argThat(new TtlDeploymentSubmitted(false, 2, 0, 0, platform_1)), Mockito.eq(esRecord1.getId()));
        Mockito.verify(searchDal).put(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Matchers.argThat(new TtlDeploymentSubmitted(false, 2, 0, 0, platform_2)), Mockito.eq(esRecord2.getId()));

        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

        //2.end

        //3. on third time, it should actually go ahead and submit ttl deployment for first platform but not second
        Mockito.reset(ooFacade);
        Mockito.reset(searchDal);

        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_1EsQuery)))
                .thenReturn(ttlRecordList1);

        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_2EsQuery)))
                .thenReturn(ttlRecordList2);

        ttlRecord1.setUserNotifiedTimes(2);
        ttlRecord2.setUserNotifiedTimes(2);

        ttlPlugin.processEnvironment(env, deployments, orgs);

        Mockito.verify(searchDal).put(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Matchers.argThat(new TtlDeploymentSubmitted(true, 2, 0, 0, platform_1)), Mockito.eq(esRecord1.getId()));
        Mockito.verify(searchDal, Mockito.times(0)).put(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Matchers.argThat(new TtlDeploymentSubmitted(false, 2, 0, 0, platform_2)), Mockito.eq(esRecord2.getId()));

        Mockito.verify(ooFacade, Mockito.times(1)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        //second platform should not be disabled in the same run
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(1)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        //second platform should not be deployed in the same run
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

        //#3.end

        //4. Assert the reclaimedCores and reclaimedComputes are set correctly
        Mockito.reset(ooFacade);
        Mockito.reset(searchDal);
        platform_1.setEnable("disable");
        platform_1.setTotalCores(0);
        platform_1.setTotalComputes(0);
        Platform platformInEs = new Platform();
        platformInEs.setTotalCores(40);
        platformInEs.setTotalComputes(10);
        platformInEs.setId(platform_1.getId());
        ttlRecord1.setPlatform(platformInEs);
        ttlRecord1.setTtlDeploymentSubmitted(true);

        platform_1EsQuery = platform_1EsQuery.replace("false", "true");
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_1EsQuery)))
                .thenReturn(ttlRecordList1);

        ttlPlugin.processEnvironment(env, deployments, orgs);

        Mockito.verify(searchDal).put(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Matchers.argThat(new TtlDeploymentSubmitted(true, 2
                        , 40, 10, platform_1)), Mockito.eq(esRecord1.getId()));

    }

    @Test
    public void testNonCompleteDeployment() throws Exception {
        //set the destroyDate as past-due and make sure it gets ttled on second scan
        // because the grace period is set to 0 days for this test
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2); //set planned destroy date as past-due
        Date destroyDate = calendar.getTime();
        EnvironmentTTLRecord ttlRecord = new EnvironmentTTLRecord();
        ttlRecord.setPlannedDestroyDate(destroyDate);
        ttlRecord.setUserNotifiedTimes(2);
        ESRecord esRecord = new ESRecord();
        esRecord.setSource(ttlRecord);
        deployment.setState("pausing");
        List<ESRecord> ttlRecordList = new ArrayList<>();

        ttlRecordList.add(esRecord);
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -150);//last deployment 150 days back
        deployment.setCreatedAt(cal.getTime());

        String platform_1EsQuery = esQuery.toString()
                .replace("<platformId>", "" + platform_1.getId())
                .replace("<ttlDeploymentSubmitted>", String.valueOf(false));
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_1EsQuery)))
                .thenReturn(ttlRecordList);

        String platform_2EsQuery = esQuery.toString()
                .replace("<platformId>", "" + platform_2.getId())
                .replace("<ttlDeploymentSubmitted>", String.valueOf(false));
        Mockito.when(searchDal.search(Mockito.eq(ttlPlugin.getIndexName()), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq(platform_2EsQuery)))
                .thenReturn(ttlRecordList);

        env.setProfile("QA");
        Mockito.reset(ooFacade);
        Mockito.reset(searchDal);

        ttlPlugin.processEnvironment(env, deployments, new HashMap<>());

        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(Mockito.anyObject(), Mockito.eq(ttlPlugin.ttlBotName));
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(Mockito.eq(env), Mockito.anyObject(), Mockito.eq(ttlPlugin.ttlBotName));
    }


    private static final class TtlDeploymentSubmitted extends ArgumentMatcher<EnvironmentTTLRecord> {
        boolean ttlSubmitted;
        int userNotifiedTimes;
        int coresReclaimed;
        int computesReclaimed;
        Platform platform;

        public TtlDeploymentSubmitted(boolean ttlSubmitted, int userNotifiedTimes, int coresReclaimed,
                                      int computesReclaimed, Platform platform) {
            this.ttlSubmitted = ttlSubmitted;
            this.userNotifiedTimes = userNotifiedTimes;
            this.coresReclaimed = coresReclaimed;
            this.computesReclaimed = computesReclaimed;
            this.platform = platform;
        }

        @Override
        public boolean matches(Object argument) {
            EnvironmentTTLRecord ttlRecord = ((EnvironmentTTLRecord) argument);
            boolean ttlSubmitted =  (ttlRecord.getTtlDeploymentSubmitted() == this.ttlSubmitted);
            boolean platformIdMatches = (platform.getId() == ttlRecord.platform.getId());
            boolean userNotifiedMatches = (this.userNotifiedTimes == ttlRecord.getUserNotifiedTimes());
            boolean coresReclaimedMatches = (this.coresReclaimed == ttlRecord.getReclaimedCores());
            boolean computesReclaimedMatches = (this.computesReclaimed == ttlRecord.getReclaimedComputes());
            return (ttlSubmitted && platformIdMatches && userNotifiedMatches
                    && coresReclaimedMatches && computesReclaimedMatches);
        }
    }
}



