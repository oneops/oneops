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
package com.oneops.cms.util;

import org.apache.log4j.Logger;

import java.util.Map;

import static com.oneops.cms.util.CmsUtil.*;

/**
 * Holds the context used for variable inerpolation
 */
public class VariableContext {
    private final long ciId;
    private final String ciName;
    private final String nsPath;
    private  String attrName;
    private  String unresolvedAttrValue;
    private final Map<String, String> cloudVars;
    private final Map<String, String> globalVars;
    private final Map<String, String> localVars;

    private static final Logger logger = Logger.getLogger(VariableContext.class);

    public VariableContext(long ciId, String ciName, String nsPath,  Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
        this.ciId = ciId;
        this.ciName = ciName;
        this.nsPath = nsPath;
        this.cloudVars = cloudVars;
        this.globalVars = globalVars;
        this.localVars = localVars;
    }



    public long getCiId() {
        return ciId;
    }

    public String getCiName() {
        return ciName;
    }

    public String getNsPath() {
        return nsPath;
    }

    public String getAttrName() {
        return attrName;
    }

    public String getUnresolvedAttrValue() {
        return unresolvedAttrValue;
    }

    public Map<String, String> getCloudVars() {
        return cloudVars;
    }

    public Map<String, String> getGlobalVars() {
        return globalVars;
    }

    public Map<String, String> getLocalVars() {
        return localVars;
    }


    public String getCloudVar(String variableToResolve) {
        String cloudVarValue = null;
        if (getCloudVars() != null && getCloudVars().get("cloud_name") != null)
            cloudVarValue = getCloudSystemVarValue(getCloudVars().get("cloud_name"), variableToResolve);
        if (cloudVarValue == null && getCloudVars() != null)
            cloudVarValue = getCloudVars().get(variableToResolve);
        return cloudVarValue;
    }

    public String getGlobalVar(String variableToResolve) {
        if (getGlobalVars() != null )
            return  getGlobalVars().get(variableToResolve);
        return null;
    }
    public String getLocalVar(String variableToResolve) {
        if (getLocalVars() != null )
            return  getLocalVars().get(variableToResolve);
        return null;
    }





    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public void setUnresolvedAttrValue(String unresolvedAttrValue) {
        this.unresolvedAttrValue = unresolvedAttrValue;
    }

    public String get(String varName, String prefix) {
        String varValue = null;
        if (prefix!=null){
            switch (prefix) {
                case CLOUDVARPFX:
                    varValue= getCloudVar(varName);
                    break;
                case GLOBALVARPFX:
                    varValue= getGlobalVar(varName);
                    break;
                case LOCALVARPFX:
                    varValue = getLocalVar(varName);
                    break;
                default:
                    varValue = null;

            }
        }
        return varValue;
    }


}
