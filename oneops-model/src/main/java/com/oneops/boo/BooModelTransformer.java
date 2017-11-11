package com.oneops.boo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.oneops.Assembly;
import com.oneops.Attachment;
import com.oneops.Cloud;
import com.oneops.Component;
import com.oneops.Environment;
import com.oneops.OneOps;
import com.oneops.Platform;

public class BooModelTransformer {

  public OneOps convert(BooYaml booYaml) {
    OneOps oneops = new OneOps();

    if (booYaml != null) {
      Boo boo = booYaml.getBoo();
      oneops.setApikey(boo.getApi_key());
      oneops.setHost(boo.getOneops_host());
      oneops.setOrganization(boo.getOrganization());
      Assembly assembly = convert(booYaml.getAssembly());
      assembly.setEmail(booYaml.getBoo().getEmail());

      Map<String, Platform> platforms = convertPlatforms(booYaml.getPlatformList());


      assembly.setPlatforms(platforms);

      Map<String, Environment> environments = convertEnvironments(booYaml.getEnvironmentList());

      if (booYaml.getScale() != null) {
        List<BooScale> booScaleList = booYaml.getScaleList();
        if (booScaleList != null) {
          for (BooScale booScale : booScaleList) {
            for (Entry<String, Environment> environmentEntry : environments.entrySet()) {
              if (environmentEntry != null && environmentEntry.getValue() != null && environmentEntry.getValue().getPlatforms() != null) {
                for (Entry<String, Platform> platformEntry : environmentEntry.getValue().getPlatforms().entrySet()) {
                  if (booScale.getPlatformName() != null && booScale.getPlatformName().equals(platformEntry.getKey())) {
                    Platform platform = platformEntry.getValue();
                    platform.setScale(booYaml.getScale().get(booScale.getPlatformName()).get("scaling"));
                  }
                }
              }
            }
          }
        }
      }

      assembly.setEnvironments(environments);
      oneops.setAssembly(assembly);
    }
    return oneops;
  }

  private Assembly convert(BooAssembly booAssembly) {
    Assembly assembly = new Assembly();

    if (booAssembly != null) {
      assembly.setName(booAssembly.getName());
      assembly.setDescription(booAssembly.getDescription());
      assembly.setTags(booAssembly.getTags());
      assembly.setAutogen(String.valueOf(booAssembly.getAuto_gen()));
    }

    return assembly;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Platform> convertPlatforms(List<BooPlatform> booPlatformList) {
    Map<String, Platform> platformMap = Maps.newHashMap();
    if (booPlatformList != null) {
      for (BooPlatform booPlatform : booPlatformList) {
        Platform platform = new Platform();
        platform.setId(booPlatform.getName());
        platform.setPack(booPlatform.getPack());
        platform.setPackVersion(booPlatform.getPack_version());
        platform.setSource(booPlatform.getSource());
        platform.setVariables(booPlatform.getVariables());
        platform.setEncryptedvariables(booPlatform.getEncrypted_variables());
        platform.setConfiguration(booPlatform.getAuto_healing());
        platform.setScale(booPlatform.getScale());
        platform.setLinks(booPlatform.getLinks());
        Map<String, Map<String, Object>> boocomponents = booPlatform.getComponents();
        if (boocomponents != null) {
          Map<String, Component> components = Maps.newLinkedHashMap();
          for (Entry<String, Map<String, Object>> entry : boocomponents.entrySet()) {
            Component component = new Component();
            component.setType(entry.getKey());
            for (Entry<String, Object> entry0 : entry.getValue().entrySet()) {
              if (entry0.getValue() instanceof Map) {
                if ("attachments".equals(entry0.getKey())) {
                  Map<String, Attachment> attachments = Maps.newHashMap();

                  Map<String, Object> config = (Map<String, Object>) entry0.getValue();
                  for (Entry<String, Object> attachmentEntry : config.entrySet()) {
                    Attachment attachment = new Attachment();
                    if (attachmentEntry != null && attachmentEntry.getValue() instanceof Map) {
                      Map<String, String> attach = (Map<String, String>) attachmentEntry.getValue();
                      attachment.setConfiguration(attach);
                      attachment.setId(attachmentEntry.getKey());
                      attachments.put(attachmentEntry.getKey(), attachment);
                    }
                  }

                  component.setAttachments(attachments);
                } else { //optional component
                  component = new Component();
                  component.setType(entry.getKey());
                  Map<String, Object> config = (Map<String, Object>) entry0.getValue();
                  component.setId(entry0.getKey());
                  if (config.containsKey("sibling_depends_on")) {
                    Map<String, String> siblingLink = (Map<String, String>) entry0.getValue();
                    String linkStr = siblingLink.get("sibling_depends_on");
                    if (linkStr != null) {
                      List<String> compList = Arrays.asList(linkStr.replace("[", "").replace("]", "").replaceAll("\"", "").split(","));
                      component.setLinks(compList);
                    }
                    config.remove("sibling_depends_on");
                  }
                  if (config.containsKey("attachments")) {
                    Map<String, Attachment> attachments = Maps.newHashMap();

                    Map<String, Object> value = (Map<String, Object>) config.get("attachments");
                    for (Entry<String, Object> attachmentEntry : value.entrySet()) {
                      Attachment attachment = new Attachment();
                      if (attachmentEntry != null && attachmentEntry.getValue() instanceof Map) {
                        Map<String, String> attach = (Map<String, String>) attachmentEntry.getValue();
                        attachment.setConfiguration(attach);
                        attachment.setId(attachmentEntry.getKey());
                        attachments.put(attachmentEntry.getKey(), attachment);
                      }
                    }
                    component.setAttachments(attachments);
                    config.remove("attachments");
                  }

                  Map<String, String> finalConfigs = Maps.newHashMap();
                  for (Entry<String, Object> configEntry : config.entrySet()) {
                    finalConfigs.put(configEntry.getKey(), String.valueOf(configEntry.getValue()));
                  }
                  component.setConfiguration(finalConfigs);
                }
                components.put(component.getId(), component);
              } else {
                component.setId(entry.getKey());
                Map<String, String> map = Maps.newHashMap();
                map.put(entry0.getKey(), String.valueOf(entry0.getValue()));
                component.setConfiguration(map);
                components.put(component.getId(), component);
              }
            }


          }
          platform.setComponents(components);
        }
        platformMap.put(booPlatform.getName(), platform);
      }
    }

    return platformMap;
  }

  private Map<String, Cloud> convertClouds(List<BooCloud> booCloudList) {
    Map<String, Cloud> cloudMap = Maps.newHashMap();
    if (booCloudList != null) {
      for (BooCloud booCloud : booCloudList) {
        Cloud cloud = new Cloud();
        cloud.setDeploymentorder(Integer.parseInt(booCloud.getDpmt_order()));
        cloud.setPriority(Integer.parseInt(booCloud.getPriority()));
        cloud.setScalepercentage(Integer.parseInt(booCloud.getPct_scale()));
        cloud.setId(booCloud.getName());
        cloudMap.put(booCloud.getName(), cloud);
      }
    }

    return cloudMap;
  }

  private Map<String, Environment> convertEnvironments(List<BooEnvironment> booEnvironmentList) {
    Map<String, Environment> environmentMap = Maps.newHashMap();
    if (booEnvironmentList != null) {
      for (BooEnvironment booEnvironment : booEnvironmentList) {
        Environment environment = new Environment();
        environment.setId(booEnvironment.getName());
        environment.setAvailability(booEnvironment.getAvailability());
        environment.setGlobaldns(Boolean.getBoolean(booEnvironment.getGlobal_dns()));
        environment.setProfile(booEnvironment.getProfile());
        environment.setClouds(convertClouds(booEnvironment.getCloudList()));
        environment.setPlatforms(convertPlatforms(booEnvironment.getPlatformList()));

        environmentMap.put(booEnvironment.getName(), environment);
      }
    }

    return environmentMap;
  }
}
