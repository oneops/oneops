package com.oneops.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.yaml.snakeyaml.Yaml;

public class OneOpsYamlReader {

  public <T> T read(File yamlfile, Class<T> yamlModelClass) throws FileNotFoundException {
    Yaml yaml = new Yaml();
    T yamlmodel = yaml.loadAs(new FileInputStream(yamlfile), yamlModelClass);
    return yamlmodel;
  }

}
