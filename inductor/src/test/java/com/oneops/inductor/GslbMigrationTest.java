package com.oneops.inductor;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.gslb.GslbProvider;
import com.oneops.inductor.util.ResourceUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/** A standalone gslb migration test. */
@Ignore
public class GslbMigrationTest {

  @Test
  public void migration() {

    Gson gson = new Gson();

    WoHelper wh = new WoHelper();
    wh.gson = gson;

    FqdnVerifier fqdnVerifier = new FqdnVerifier();
    fqdnVerifier.woHelper = wh;

    FqdnExecutor fqdnExec = new FqdnExecutor();
    fqdnExec.gslbProvider = new GslbProvider();
    fqdnExec.jsonParser = new JsonParser();
    fqdnExec.woHelper = wh;
    fqdnExec.fqdnVerifier = fqdnVerifier;
    fqdnExec.gson = gson;

    CmsActionOrderSimple ao =
        gson.fromJson(
            ResourceUtils.readResourceAsString("/gslb-migration.json"), CmsActionOrderSimple.class);
    Response res = fqdnExec.execute(ao, System.getProperty("tmp.dir"));
    Assert.assertEquals(OpsActionState.complete, ao.getActionState());
  }
}
