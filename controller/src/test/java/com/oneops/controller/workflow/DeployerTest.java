package com.oneops.controller.workflow;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.controller.cms.CMSClient;
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

  private void resetDeploymentData(long dpmtId) throws Exception {
    Connection conn = ds.getConnection();
    try (PreparedStatement stmt = conn.prepareStatement("UPDATE dj_deployment_rfc SET state_id = 10 where deployment_id = ?")) {
      stmt.setLong(1, dpmtId);
      stmt.executeUpdate();
    }
    try (PreparedStatement stmt = conn.prepareStatement("UPDATE dj_deployment SET state_id = 100, "
        + "auto_pause_exec_orders = null, current_step = null, flags = 0 where deployment_id = ?")) {
      stmt.setLong(1, dpmtId);
      stmt.executeUpdate();
    }
  }

  @Test
  public void testProcessWo() throws Exception {
    long dpmtId = 73078;
    resetDeploymentData(73078);

    //step:workorders [1:1, 2:1, 3:3, 4:4, 5:6]
    processWorkOrders(dpmtId);
    verifyWo(1, 1);
    completeWos(dpmtId, 1, 1);

    processWorkOrders(dpmtId);
    verifyWo(2, 1);
    completeWos(dpmtId, 2, 1);

    processWorkOrders(dpmtId);
    verifyWo(3, 3);
    completeWos(dpmtId, 3, 3);

    processWorkOrders(dpmtId);
    verifyWo(4, 4);
    completeWos(dpmtId, 4, 4);

    processWorkOrders(dpmtId);
    verifyWo(5, 6);
  }


  @Test
  public void testConverge() throws Exception {
    long dpmtId = 73078;
    resetDeploymentData(dpmtId);
    processWorkOrders(dpmtId);
    verifyWo(1, 1);
    sendInductorResponse(dpmtId, 73080, 72824, 1, CMSClient.COMPLETE);
    verifyConverge(dpmtId);

    processWorkOrders(dpmtId);
    verifyWo(2, 1);
    sendInductorResponse(dpmtId, 73081, 72817, 2, CMSClient.COMPLETE);
    verifyConverge(dpmtId);

    processWorkOrders(dpmtId);
    verifyWo(3, 3);
    sendInductorResponse(dpmtId, 73082, 72830, 3, CMSClient.COMPLETE);
    sendInductorResponse(dpmtId, 73084, 72852, 3, CMSClient.COMPLETE);
    sendInductorResponse(dpmtId, 73083, 72841, 3, CMSClient.COMPLETE);
    verifyConverge(dpmtId);
  }


  @Test
  public void testAutoPause() throws Exception {
    long dpmtId = 73078;
    resetDeploymentData(dpmtId);
    setAutoPause(dpmtId, "2,4");
    processWorkOrders(dpmtId);
    verifyWo(1, 1);
    completeWos(dpmtId, 1, 1);
    processWorkOrders(dpmtId);
    verifyDpmtState(dpmtId, CmsDpmtProcessor.DPMT_STATE_PAUSED);
  }

  @Test
  public void testDeploymentFailure() throws Exception {
    long dpmtId = 73078;
    resetDeploymentData(dpmtId);
    processWorkOrders(dpmtId);
    verifyWo(1, 1);
    sendInductorResponse(dpmtId, 73081, 72817, 2, CMSClient.FAILED);
    verifyDpmtState(dpmtId, CmsDpmtProcessor.DPMT_STATE_FAILED);
  }

  @Test
  public void testContinueOnFailure() throws Exception {
    long dpmtId = 73078;
    resetDeploymentData(dpmtId);
    enableContinueOnFailure(dpmtId);
    processWorkOrders(dpmtId);
    verifyWo(1, 1);
    sendInductorResponse(dpmtId, 73081, 72817, 2, CMSClient.FAILED);
    processWorkOrders(dpmtId);
    verifyWo(2, 1);
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


  private void verifyDpmtState(long dpmtId, String state) {
    CmsDeployment dpmt = dpmtProessor.getDeployment(dpmtId);
    Assert.assertEquals(dpmt.getDeploymentState(), state);
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
