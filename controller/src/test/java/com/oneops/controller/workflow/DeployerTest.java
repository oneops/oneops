package com.oneops.controller.workflow;

import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_ACTIVE;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_FAILED;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_PAUSED;
import static com.oneops.controller.cms.CMSClient.COMPLETE;
import static com.oneops.controller.cms.CMSClient.FAILED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.workflow.WorkflowPublisher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
@ContextConfiguration(classes = DeployerConfiguration.class, loader = AnnotationConfigContextLoader.class)
@TestPropertySource(properties = {
    "oo.controller.wo.assembler.pool.size=1", "oo.controller.wo.async.threshold=50"
})
public class DeployerTest extends AbstractTestNGSpringContextTests {

  private static final String COMPLETE_WO_QUERY =
      "UPDATE dj_deployment_rfc SET state_id = 200 WHERE deployment_rfc_id in (\n" +
          "  SELECT d.deployment_rfc_id\n" +
          "  FROM dj_deployment_rfc d, dj_rfc_ci r\n" +
          "  WHERE d.deployment_id = ?\n" +
          "   AND d.rfc_id = r.rfc_id\n" +
          "   AND d.state_id in (10, 100)\n" +
          "   AND r.execution_order = ? LIMIT ? );";

  @Autowired
  private DeployerImpl deployer;

  @Autowired
  ThreadPoolExecutor mockExecutor;

  @Autowired
  CmsDpmtProcessor dpmtProessor;

  @Autowired
  DataSource ds;

  @Autowired
  WorkflowPublisher workflowPublisher;

  @BeforeClass
  public void setup() {
    deployer.setWoDispatchExecutor(mockExecutor);
  }

  private final  Long DPMT_ID = 73078l;

  @BeforeMethod
  private void resetDeploymentData() throws Exception {
    Connection conn = ds.getConnection();
    try (PreparedStatement stmt = conn.prepareStatement("UPDATE dj_deployment_rfc SET state_id = 10 where deployment_id = ?")) {
      stmt.setLong(1, DPMT_ID);
      stmt.executeUpdate();
    }
    try (PreparedStatement stmt = conn.prepareStatement("UPDATE dj_deployment SET state_id = 100, "
        + "auto_pause_exec_orders = null, current_step = null, flags = 0 where deployment_id = ?")) {
      stmt.setLong(1, DPMT_ID);
      stmt.executeUpdate();
    }
    reset(mockExecutor);
  }

  @Test
  public void testProcessWo() throws Exception {

    //step:workorders [1:1, 2:1, 3:3, 4:4, 5:6]
    processWorkOrders(DPMT_ID);
    verifyWo(1, 1);
    completeWos(DPMT_ID, 1, 1);

    processWorkOrders(DPMT_ID);
    verifyWo(2, 1);
    completeWos(DPMT_ID, 2, 1);

    processWorkOrders(DPMT_ID);
    verifyWo(3, 3);
    completeWos(DPMT_ID, 3, 3);

    processWorkOrders(DPMT_ID);
    verifyWo(4, 4);
    completeWos(DPMT_ID, 4, 4);

    processWorkOrders(DPMT_ID);
    verifyWo(5, 6);
  }


  @Test
  public void testConverge() throws Exception {
    processWorkOrders(DPMT_ID);
    verifyWo(1, 1);
    sendInductorResponse(DPMT_ID, 73080, 72824, 1, COMPLETE);
    verifyConverge(DPMT_ID);

    processWorkOrders(DPMT_ID);
    verifyWo(2, 1);
    sendInductorResponse(DPMT_ID, 73081, 72817, 2, COMPLETE);
    verifyConverge(DPMT_ID);

    processWorkOrders(DPMT_ID);
    verifyWo(3, 3);
    sendInductorResponse(DPMT_ID, 73082, 72830, 3, COMPLETE);
    sendInductorResponse(DPMT_ID, 73084, 72852, 3, COMPLETE);
    sendInductorResponse(DPMT_ID, 73083, 72841, 3, COMPLETE);
    verifyConverge(DPMT_ID);
  }


  @Test
  public void testAutoPause() throws Exception {
    setAutoPause(DPMT_ID, "2,4");
    processWorkOrders(DPMT_ID);
    verifyWo(1, 1);
    completeWos(DPMT_ID, 1, 1);
    processWorkOrders(DPMT_ID);
    verifyDpmtState(DPMT_ID, DPMT_STATE_PAUSED);
  }

  @Test
  public void testDeploymentFailure() throws Exception {
    processWorkOrders(DPMT_ID);
    verifyWo(1, 1);
    sendInductorResponse(DPMT_ID, 73080, 72824, 1, FAILED);
    verifyDpmtState(DPMT_ID, DPMT_STATE_FAILED);
    processWorkOrders(DPMT_ID);
    verifyWo(1, 0);
  }

  @Test
  public void testContinueOnFailure() throws Exception {
    enableContinueOnFailure(DPMT_ID);
    processWorkOrders(DPMT_ID);
    verifyWo(1, 1);
    sendInductorResponse(DPMT_ID, 73080, 72824, 1, FAILED);
    processWorkOrders(DPMT_ID);
    verifyWo(2, 1);
  }

  @Test
  public void testRetryDeployment() throws Exception {
    processWorkOrders(DPMT_ID);
    verifyWo(1, 1);
    sendInductorResponse(DPMT_ID, 73080, 72824, 1, COMPLETE);
    processWorkOrders(DPMT_ID);
    verifyWo(2, 1);
    sendInductorResponse(DPMT_ID, 73081, 72817, 2, FAILED);
    CmsDeployment dpmt = verifyDpmtState(DPMT_ID, DPMT_STATE_FAILED);
    //retry the deployment
    dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
    dpmtProessor.updateDeployment(dpmt);
    processWorkOrders(DPMT_ID);
    verifyWo(2, 1);
    sendInductorResponse(DPMT_ID, 73081, 72817, 2, COMPLETE);
    processWorkOrders(DPMT_ID);
    verifyWo(3, 3);
  }

  @Test
  public void testPauseResume() throws Exception {
    processWorkOrders(DPMT_ID);
    sendInductorResponse(DPMT_ID, 73080, 72824, 1, COMPLETE);
    processWorkOrders(DPMT_ID);
    sendInductorResponse(DPMT_ID, 73081, 72817, 2, COMPLETE);
    processWorkOrders(DPMT_ID);
    sendInductorResponse(DPMT_ID, 73082, 72830, 3, COMPLETE);
    sendInductorResponse(DPMT_ID, 73084, 72852, 3, COMPLETE);
    sendInductorResponse(DPMT_ID, 73083, 72841, 3, COMPLETE);
    CmsDeployment dpmt = verifyDpmtState(DPMT_ID, DPMT_STATE_ACTIVE);
    dpmt.setDeploymentState(DPMT_STATE_PAUSED);
    dpmtProessor.updateDeployment(dpmt);
    reset(mockExecutor);
    processWorkOrders(DPMT_ID);
    verifyWo(4, 0);
    dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
    dpmtProessor.updateDeployment(dpmt);
    processWorkOrders(DPMT_ID);
    verifyWo(4, 4);
  }

  private void enableContinueOnFailure(long dpmtId) throws Exception {
    Connection conn = ds.getConnection();
    try (PreparedStatement stmt = conn.prepareStatement("UPDATE dj_deployment SET flags =1 where deployment_id = ?")) {
      stmt.setLong(1, dpmtId);
      stmt.executeUpdate();
    }
  }

  private void sendInductorResponse(long dpmtId, long dpmtRecordId, long rfcId, int step, String state) throws Exception {
    CmsWorkOrderSimple woResp = new CmsWorkOrderSimple();
    woResp.setDeploymentId(dpmtId);
    woResp.setDpmtRecordId(dpmtRecordId);
    woResp.setDpmtRecordState(state);
    woResp.setRfcId(rfcId);
    CmsRfcCISimple rfcCi = new CmsRfcCISimple();
    rfcCi.setRfcId(rfcId);
    rfcCi.setExecOrder(step);
    rfcCi.setRfcAction("add");
    woResp.setRfcCi(rfcCi);
    Map<String, Object> params = new HashMap<>();
    rfcCi.setNsPath("/test1/c1/dev/bom/t1/1");
    params.put("wostate", state);
    deployer.handleInductorResponse(woResp, params);
  }

  private void verifyConverge(long dpmtId) {
    try {
      verify(workflowPublisher).sendWorkflowMessage(dpmtId, null);
      reset(workflowPublisher);
    } catch(Exception e) {
      Assert.fail("failed verifying converge");
    }
  }


  private CmsDeployment verifyDpmtState(long dpmtId, String state) {
    CmsDeployment dpmt = dpmtProessor.getDeployment(dpmtId);
    Assert.assertEquals(dpmt.getDeploymentState(), state);
    return dpmt;
  }

  private CmsDeployment verifyDpmtCurrentStep(long dpmtId, int step) {
    CmsDeployment dpmt = dpmtProessor.getDeployment(dpmtId);
    Assert.assertEquals(dpmt.getCurrentStep(), step);
    return dpmt;
  }

  private void setAutoPause(long dpmtId, String steps) throws Exception {
    Connection conn = ds.getConnection();
    try (PreparedStatement stmt = conn.prepareStatement("UPDATE dj_deployment SET auto_pause_exec_orders = ? where deployment_id = ?")) {
      stmt.setString(1, steps);
      stmt.setLong(2, dpmtId);
      stmt.executeUpdate();
    }
  }

  private void completeWos(long dpmtId, int step, int limit) throws Exception {
    Connection conn = ds.getConnection();
    try (PreparedStatement stmt = conn.prepareStatement(COMPLETE_WO_QUERY)) {
      stmt.setLong(1, dpmtId);
      stmt.setInt(2, step);
      stmt.setInt(3, limit);
      stmt.executeUpdate();
    }
  }

  private void verifyWo(int step, int woCount) {
    verify(mockExecutor, times(woCount)).submit(any(Runnable.class));
    reset(mockExecutor);
  }

  private void processWorkOrders(long dpmtId) {
    deployer.processWorkOrders(dpmtId, false);
  }

}
