package com.oneops.crawler.plugins.quota;

import com.oneops.Platform;
import com.oneops.crawler.ThanosClient;

import java.io.Serializable;
import java.util.List;

/*******************************************************************************
 *
 *   Copyright 2018 Walmart, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *******************************************************************************/
public class PlatformRecord implements Serializable {
    int potentialVmReclaimCount;
    int potentialCoresReclaimCount;
    Platform platform;
    List<ThanosClient.CloudResourcesUtilizationStats> cloudResourcesUtilizationStats;

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public List<ThanosClient.CloudResourcesUtilizationStats>  getCloudResourcesUtilizationStats() {
        return cloudResourcesUtilizationStats;
    }

    public void setCloudResourcesUtilizationStats(List<ThanosClient.CloudResourcesUtilizationStats> cloudResourcesUtilizationStats) {
        this.cloudResourcesUtilizationStats = cloudResourcesUtilizationStats;
    }
    public int getPotentialVmReclaimCount() {
        return potentialVmReclaimCount;
    }

    public void setPotentialVmReclaimCount(int potentialVmReclaimCount) {
        this.potentialVmReclaimCount = potentialVmReclaimCount;
    }

    public int getPotentialCoresReclaimCount() {
        return potentialCoresReclaimCount;
    }

    public void setPotentialCoresReclaimCount(int potentialCoresReclaimCount) {
        this.potentialCoresReclaimCount = potentialCoresReclaimCount;
    }
}
