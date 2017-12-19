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
package com.oneops.transistor.service.peristenceless;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.*;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.CmsException;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.ns.domain.CmsNamespace;
import com.oneops.cms.ns.service.CmsNsProcessor;
import com.oneops.cms.util.*;
import com.oneops.cms.util.dal.UtilMapper;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.BASE_REALIZED_AS;

/**
 * The Class CmsCmProcessor.
 */
public class CmsCmProcessorIM extends CmsCmProcessor {
	@Override
	public void deleteCI(long ciId, boolean delete4real, String userId) {
		// do nothing for in memory bom generation we don't want to delete anything for real
	}
}

	
