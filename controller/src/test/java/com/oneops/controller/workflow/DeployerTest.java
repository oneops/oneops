package com.oneops.controller.workflow;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ThreadPoolExecutor;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
@ContextConfiguration(classes = DeployerConfiguration.class, loader = AnnotationConfigContextLoader.class)
@TestPropertySource(properties = {
    "oo.controller.wo.assembler.pool.size=1", "oo.controller.wo.async.threshold=50"
})
public class DeployerTest extends AbstractTestNGSpringContextTests {

  private static final String UPDATE_QUERY =
      "UPDATE dj_deployment_rfc SET state_id = 200 WHERE deployment_rfc_id in (\n" +
          "  SELECT d.deployment_rfc_id\n" +
          "  FROM dj_deployment_rfc d, dj_rfc_ci r\n" +
          "  WHERE d.deployment_id = ?\n" +
          "   AND d.rfc_id = r.rfc_id\n" +
          "   AND d.state_id in (10, 100)\n" +
          "   AND r.execution_order = ? LIMIT ? );";

  @Autowired
  private Deployer deployer;

  @Autowired
  private DeploymentCache dpmtCache;

  @Autowired
  ThreadPoolExecutor mockExecutor;

  @Autowired
  DataSource ds;

  @BeforeMethod
  public void setup() {
    deployer.setWoDispatchExecutor(mockExecutor);
  }

  @Test
  public void testProcessWo() {
    long dpmtId = 219664;
    DeploymentExecution dpmtExec = new DeploymentExecution();
    dpmtExec.setDeploymentId(dpmtId);
    dpmtCache.updateDeploymentMap(dpmtId, dpmtExec);

    processAndVerifyStepWorkorders(dpmtExec, 1, 3);
    processAndVerifyStepWorkorders(dpmtExec, 2, 5);
    processAndVerifyStepWorkorders(dpmtExec, 3, 30);
    processAndVerifyStepWorkorders(dpmtExec, 4, 6);
    processAndVerifyStepWorkorders(dpmtExec, 5, 5);
    processAndVerifyStepWorkorders(dpmtExec, 6, 1);
  }

  @Test
  public void testProcessWoMultiBatch() throws Exception {
    long dpmtId = 287398;
    DeploymentExecution dpmtExec = new DeploymentExecution();
    dpmtExec.setDeploymentId(dpmtId);
    dpmtCache.updateDeploymentMap(dpmtId, dpmtExec);

    //step 1 (110 workorders)
    processAndVerifyWorkorders(dpmtExec, 110);
    updateWos(dpmtExec, 1, 30);

    //only 30 got completed so next call should return 80
    processAndVerifyWorkorders(dpmtExec, 80);

    updateWos(dpmtExec, 1, 10);
    processAndVerifyWorkorders(dpmtExec, 70);

    updateWos(dpmtExec, 1, 30);
    processAndVerifyWorkorders(dpmtExec, 40);

    updateWos(dpmtExec, 1, 30);
    processAndVerifyWorkorders(dpmtExec, 10);

    updateWos(dpmtExec, 1, 10);

    //step 2 (111 workorders)
    processAndVerifyWorkorders(dpmtExec, 111);
    updateWos(dpmtExec, 2, 100);

    processAndVerifyWorkorders(dpmtExec, 11);
    updateWos(dpmtExec, 2, 11);

    //step 3 (50 workorders)
    processAndVerifyWorkorders(dpmtExec, 50);
  }

  private void updateWos(DeploymentExecution dpmtExec, int step, int limit) throws Exception {
    DeploymentStep dpmtStep = dpmtExec.getStepMap().get(dpmtExec.getCurrentStep());
    Connection conn = ds.getConnection();
    PreparedStatement stmt = conn.prepareStatement(UPDATE_QUERY);
    stmt.setLong(1, dpmtExec.getDeploymentId());
    stmt.setInt(2, step);
    stmt.setInt(3, limit);
    stmt.executeUpdate();
  }

  private void processAndVerifyStepWorkorders(DeploymentExecution dpmtExec, int step, int woCount) {
    dpmtExec.setCurrentStep(step);
    processAndVerifyWorkorders(dpmtExec, woCount);
  }

  private void processAndVerifyWorkorders(DeploymentExecution dpmtExec, int woCount) {
    deployer.processWorkOrders(dpmtExec.getDeploymentId(), false, false);
    System.out.println("step " + dpmtExec.getCurrentStep());
    verify(mockExecutor, times(woCount)).submit(any(Runnable.class));
    reset(mockExecutor);
  }

}
