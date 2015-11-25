package com.oneops.sensor;

import com.espertech.esper.client.UpdateListener;
import com.oneops.sensor.thresholds.ThresholdsDao;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/* more to do here when we can mock the JMS connection factory */
public class SensorTest {

    private Sensor baseSensor = new Sensor();

    @BeforeClass
    public void callInit() {
        try {
            baseSensor.init(1, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, UpdateListener> listeners = new HashMap<String, UpdateListener>();
        UpdateListener updateListener = null;
        listeners.put("CloseEventListener", updateListener);
        baseSensor.setListeners(listeners);//needed in allLoaded Statements test

        ThresholdsDao tdMock = (mock(ThresholdsDao.class));
        when(tdMock.getThreshold(anyLong(), anyString())).thenReturn(null); //null only for now
        baseSensor.setTsDao(tdMock);

    }


    /**
     * ensure loaded statements do not mutate *.
     */
    @Test
    public void checkAllLoadedStatements() {

        Map<String, String> outMap = baseSensor.getAllLoadedStmts();
        assert (outMap.size() > 0);
    }


    /**
     * load statements *.
     */
    @Test
    public void loadStatementsTest() {
        baseSensor.setTsDao(mock(ThresholdsDao.class));
        baseSensor.loadStatements(0, "mock-source");
        // assert(outMap.size()>0);
        //TODO anyway to check ?

    }
}
