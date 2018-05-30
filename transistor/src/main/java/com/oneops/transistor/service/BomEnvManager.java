/*******************************************************************************
 *
 *   Copyright 2015 Walmart, Inc.
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
package com.oneops.transistor.service;

import com.oneops.capacity.CapacityEstimate;
import com.oneops.transistor.service.peristenceless.BomData;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional
public interface BomEnvManager {
    void takeEnvSnapshot(long envId);

    void cleanEnvBom(long envId);

    long discardEnvBom(long envId);

    long discardEnvManifest(long envId, String userId);

    List<CostData> getEnvCostData(long envId);

    Map<String, List<CostData>> getEnvEstimatedCostData(long envId);

    Map<String, List<CostData>> getEnvEstimatedCostData(long envId, BomData data);

    Map<String, List<CapacityData>> getEnvCapacity(long envId, BomData bomData);

    CapacityEstimate estimateDeploymentCapacity(BomData bomData);
}
