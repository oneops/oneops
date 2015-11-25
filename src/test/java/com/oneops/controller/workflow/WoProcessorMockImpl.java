/*******************************************************************************
 *
 * Copyright 2015 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.controller.workflow;

import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.controller.domain.WoProcessRequest;

public class WoProcessorMockImpl implements com.oneops.controller.plugin.WoProcessor {

		public void processWo(WoProcessRequest req){
			return ;
		}
		
		public void setProcessId(Long any){
			return;
		}
		
		public void setWo(CmsWorkOrderSimple o){
			return;
		}
		
	 
}
