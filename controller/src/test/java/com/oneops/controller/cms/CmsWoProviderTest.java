package com.oneops.controller.cms;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

@Test
@ContextConfiguration(locations = { "classpath:add-test-context.xml" })
public class CmsWoProviderTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CmsWoProvider woProvider;

    Gson gson = new Gson();
	JsonParser parser = new JsonParser();

    @Test
    public void testAddWorkOrder() {

        try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("workorders/add-compute-wo.json");
			JsonObject expectedWo = (JsonObject) parser.parse(new InputStreamReader(is));
			is.close();

			CmsWorkOrderSimple workOrder = woProvider.getWorkOrderSimple(1991823, "pending", 3);
			JsonElement actual = gson.toJsonTree(workOrder);
			Assert.assertEquals(actual, expectedWo.get("compute1"));

			workOrder = woProvider.getWorkOrderSimple(1991822, "pending", 3);
			actual = gson.toJsonTree(workOrder);
			Assert.assertEquals(actual, expectedWo.get("compute2"));

			is = this.getClass().getClassLoader().getResourceAsStream("workorders/add-os-wo.json");
			expectedWo = (JsonObject) parser.parse(new InputStreamReader(is));
			is.close();
			workOrder = woProvider.getWorkOrderSimple(1991824, "pending", 4);
			actual = gson.toJsonTree(workOrder);
			Assert.assertEquals(actual, expectedWo);

			is = this.getClass().getClassLoader().getResourceAsStream("workorders/add-tomcat-wo.json");
			expectedWo = (JsonObject) parser.parse(new InputStreamReader(is));
			is.close();
			workOrder = woProvider.getWorkOrderSimple(1991832, "pending", 6);    	
			actual = gson.toJsonTree(workOrder);
			Assert.assertEquals(actual, expectedWo);
		} catch (Exception e) {
			Assert.fail("add workorder test failed ", e);
		}
    }

}
