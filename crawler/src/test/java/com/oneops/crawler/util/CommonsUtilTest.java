package com.oneops.crawler.util;

import static org.testng.Assert.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import com.oneops.crawler.CommonsUtil;
public class CommonsUtilTest {

  private final Logger log = LoggerFactory.getLogger(getClass());
  
  @Test(enabled = true)
  public void testGetIndexMappigsJsonFile() {
    
    
    String indexMappingsJsonFileName="hadrIndexMappings.json";
    try {
      CommonsUtil.getFileContent(indexMappingsJsonFileName);
    } catch (Exception e) {
      log.error("Exception while reading <hadrIndexMappings.json> contents",e);
      fail();
    }
    
  }
  
  
}
