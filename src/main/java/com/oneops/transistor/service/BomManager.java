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

import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BomManager {
	long generateBom(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit);
	long generateAndDeployBom(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit);

	long submitDeployment(long releaseId, String userId);

	void check4openDeployment(String nsPath);
}
