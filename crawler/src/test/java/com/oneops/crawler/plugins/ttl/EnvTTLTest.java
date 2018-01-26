package com.oneops.crawler.plugins.ttl;

import com.oneops.Deployment;
import com.oneops.Environment;
import com.oneops.Platform;
import com.oneops.crawler.*;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

    @BeforeClass
    public void setUp() {
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

        ArrayList<String> clouds = new ArrayList<>();
        clouds.add("cdc1");
        clouds.add("ctf3");
        platform_1.setActiveClouds(clouds);

        platform_2 = new Platform();
        platform_2.setName("platform_2");
        platform_2.setId(102);
        platform_2.setPath("/testOrg/testAssembly");
        platform_2.setTotalComputes(100);

        clouds = new ArrayList<>();
        clouds.add("testCloud");
        clouds.add("devCloud");
        platform_2.setActiveClouds(clouds);

        env.addPlatform(platform_1);
        env.addPlatform(platform_2);

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
        ttlPlugin.setTtlEnabled(true);
        ttlPlugin.setGracePeriodDays(0);
    }

    @Test
    public void testFirstScan() {

        //make sure it does not get ttled right on first scan
        ttlPlugin.processEnvironment(env, deployments);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);
    }

    @Test
    public void testPastDueTTL() {
        //now set the destroyDate as past-due and make sure it gets ttled on second scan
        // because the grace period is set to 0 days for this test
        EnvironmentTTLRecord ttlRecord = new EnvironmentTTLRecord();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -2); //set planned destroy date as past-due
        Date destroyDate = calendar.getTime();
        ttlRecord.setPlannedDestroyDate(destroyDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -150);//last deployment 150 days back
        deployment.setCreatedAt(cal.getTime());
        deployments = new ArrayList<>();
        deployments.add(deployment);

        Mockito.when(searchDal.get(Mockito.eq("oottl"), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq("" + platform_1.getId())))
                .thenReturn(ttlRecord);
        Mockito.when(searchDal.get(Mockito.eq("oottl"), Mockito.eq("platform"),
                Mockito.anyObject(), Mockito.eq("" + platform_2.getId())))
                .thenReturn(ttlRecord);
        env.setProfile("QA");
        ttlPlugin.processEnvironment(env, deployments);

        Mockito.verify(ooFacade, Mockito.times(1)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        //second platform should not be disabled in the same run
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(1)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        //second platform should not be deployed in the same run
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

        Mockito.reset(ooFacade);
        //now simulate the platform_1 is ttled already
        platform_1.setTotalComputes(0);
        ttlPlugin.processEnvironment(env, deployments);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(1)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(1)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);

    }

    @Test
    public void testActivelyDeployedEnv() {
        //now test that recently deployed env is NOT ttled
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
        ttlPlugin.processEnvironment(env, deployments);
        ttlPlugin.setOoFacade(ooFacade);

        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);
    }

    @Test
    public void testProdEnv() {
        //now test that prod env is NOT ttled
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
        ttlPlugin.processEnvironment(env, deployments);
        ttlPlugin.setOoFacade(ooFacade);

        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).disablePlatform(platform_2, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_1, ttlPlugin.ttlBotName);
        Mockito.verify(ooFacade, Mockito.times(0)).forceDeploy(env, platform_2, ttlPlugin.ttlBotName);
    }
}

