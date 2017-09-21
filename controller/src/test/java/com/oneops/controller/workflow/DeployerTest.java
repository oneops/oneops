package com.oneops.controller.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Test
@ContextConfiguration(classes=DeployerConfiguration.class, loader=AnnotationConfigContextLoader.class)
@TestPropertySource(properties = {
    "oo.controller.wo.assembler.pool.size=1", "oo.controller.wo.async.threshold=50"
})
public class DeployerTest extends AbstractTestNGSpringContextTests {

    private static final String UPDATE_QUERY = "UPDATE dj_deployment_rfc SET state_id = 200 where deployment_rfc_id in (\n" +
            "  SELECT deployment_rfc_id\n" +
            "  FROM dj_deployment_rfc d \n" +
            "  WHERE d.deployment_id = ?\n" +
            "   AND d.deployment_rfc_id in (SELECT * FROM TABLE(X INT=?) ) )";

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

        //wo limit 100
        //step 1 (110 workorders)
        processAndVerifyWorkorders(dpmtExec, 100);
        updateWos(dpmtExec,30);

        //only 30 got completed so next call should return 70
        processAndVerifyWorkorders(dpmtExec, 70);

        updateWos(dpmtExec,10);
        processAndVerifyWorkorders(dpmtExec, 60);

        updateWos(dpmtExec,30);
        processAndVerifyWorkorders(dpmtExec, 30);

        updateWos(dpmtExec,30);
        processAndVerifyWorkorders(dpmtExec, 10);

        updateWos(dpmtExec,10);

        //step 2 (111 workorders)
        processAndVerifyWorkorders(dpmtExec, 100);
        updateWos(dpmtExec,100);

        processAndVerifyWorkorders(dpmtExec, 11);
        updateWos(dpmtExec,11);

        //step 3 (50 workorders)
        processAndVerifyWorkorders(dpmtExec, 50);
    }

    private void updateWos(DeploymentExecution dpmtExec, int limit) throws Exception {
        DeploymentStep dpmtStep = dpmtExec.getStepMap().get(dpmtExec.getCurrentStep() + ":" + dpmtExec.getBatchNumber());
        List<Long> records = dpmtStep.getDpmtRecordIds().stream().limit(limit).collect(Collectors.toList());
        Connection conn = ds.getConnection();
        PreparedStatement stmt = conn.prepareStatement(UPDATE_QUERY);
        stmt.setLong(1, dpmtExec.getDeploymentId());
        stmt.setObject(2, records.toArray());
        stmt.executeUpdate();
    }

    private void updateWos(long dpmtId, int execOrder, int batch) throws Exception {
        PreparedStatement stmt = ds.getConnection().prepareStatement(UPDATE_QUERY);
        stmt.setLong(1, dpmtId);
        stmt.setInt(2, execOrder);
        stmt.setInt(3, batch);
        stmt.executeUpdate();
    }

    private void processAndVerifyStepWorkorders(DeploymentExecution dpmtExec, int step, int woCount) {
        dpmtExec.setCurrentStep(step);
        processAndVerifyWorkorders(dpmtExec, woCount);
    }

    private void processAndVerifyWorkorders(DeploymentExecution dpmtExec, int woCount) {
        deployer.processWorkOrders(dpmtExec.getDeploymentId(), false, false);
        verify(mockExecutor, times(woCount)).submit(any(Runnable.class));
        reset(mockExecutor);
    }

}
