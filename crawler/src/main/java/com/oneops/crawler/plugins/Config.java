package com.oneops.crawler.plugins;

import java.util.HashMap;

public class Config {
    boolean enabled;
    String[] orgs;
    String[] packs;

    HashMap<String, String> customConfigs;

    public String[] getOrgs() {
        return orgs;
    }

    public void setOrgs(String[] orgs) {
        this.orgs = orgs;
    }

    public String[] getPacks() {
        return packs;
    }

    public void setPacks(String[] packs) {
        this.packs = packs;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HashMap<String, String> getCustomConfigs() {
        return customConfigs;
    }

    public void setCustomConfigs(HashMap<String, String> customConfigs) {
        this.customConfigs = customConfigs;
    }
}
