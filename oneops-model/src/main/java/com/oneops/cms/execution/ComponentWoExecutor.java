package com.oneops.cms.execution;

import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public interface ComponentWoExecutor {

  public List<String> getComponentClasses();

  public Response execute(CmsWorkOrderSimple wo, String dataDir);

  public Response verify(CmsWorkOrderSimple wo, Response response);

  public default Response executeAndVerify(CmsWorkOrderSimple wo, String dataDir) {
    Response response = execute(wo, dataDir);
    if (response.getResult() == Result.SUCCESS) {
      response = verify(wo, response);
    }
    return response;
  }

  public default void writeRequest(String woJson, String fileName) {
    try (BufferedWriter out = Files.newBufferedWriter(Paths.get(fileName))) {
      out.write(woJson);
    } catch (IOException e) {
    }
  }

  public Response execute(CmsActionOrderSimple ao);

}