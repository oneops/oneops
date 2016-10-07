/*
package com.oneops.cms.util.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.VariableContext;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

*/
/**
 * Created by glall on 9/28/16.
 *//*

public class VariableProcessor {

    private CmsRfcCI newRfcVar(String name, String className, String value) {
        CmsRfcCI var = new CmsRfcCI();
        var.setCiName(name);
        var.setCiClassName(className);
        CmsRfcAttribute valueAttr = new CmsRfcAttribute();
        valueAttr.setAttributeName("value");
        valueAttr.setNewValue(value);
        var.addAttribute(valueAttr);
        return var;
    }

    public Map<String,String> getGlobalVars(CmsCI env) {
        return getVarValuesMap(getGlobalVarsRfcs(env));
    }

    public List<CmsRfcCI> getGlobalVarsRfcs(CmsCI env) {
        List<CmsRfcCI> vars = new ArrayList<CmsRfcCI>();
        CmsRfcCI envNameVar = newRfcVar("env_name","manifest.Globalvar", env.getCiName());
        vars.add(envNameVar);
        List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(env.getCiId(), "manifest.ValueFor", null);

        for (CmsCIRelation varRel : varRels) {
            vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
        }
        return vars;
    }

    public Map<String,String> getLocalVars(CmsCI plat) {
        return getVarValuesMap(getLocalVarsRfcs(plat));
    }

    public List<CmsRfcCI> getLocalVarsRfcs(CmsCI plat) {
        List<CmsRfcCI> vars = new ArrayList<CmsRfcCI>();
        CmsRfcCI platNameVar = newRfcVar("platform_name","manifest.Localvar", plat.getCiName());
        vars.add(platNameVar);

        List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(plat.getCiId(), "manifest.ValueFor", null);

        for (CmsCIRelation varRel : varRels) {
            vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
        }
        return vars;
    }


    public Map<String,String> getCloudVars(CmsCI cloud) {

        return getVarValuesMap(getCloudVarsRfcs(cloud));
    }

    public List<CmsRfcCI> getCloudVarsRfcs(CmsCI cloud) {
        List<CmsRfcCI> vars = new ArrayList<CmsRfcCI>();
        CmsRfcCI cloudNameVar = newRfcVar("cloud_name","account.Cloudvar", cloud.getCiName());
        vars.add(cloudNameVar);

        List<CmsCIRelation> varRels = cmProcessor.getToCIRelations(cloud.getCiId(), "account.ValueFor", null);

        for (CmsCIRelation varRel : varRels) {
            vars.add(rfcUtil.mergeRfcAndCi(null, varRel.getFromCi(), DJ_ATTR));
        }
        return vars;
    }

    public Map<String,String> getVarValuesMap(List<CmsRfcCI> vars) {
        Map<String,String> varsMap = new HashMap<String, String>();
        if (vars != null) {
            for (CmsRfcCI var : vars) {
                if (var.getAttribute(VAR_SEC_ATTR_FLAG) != null &&"true".equals(var.getAttribute(VAR_SEC_ATTR_FLAG).getNewValue()))  {
                    varsMap.put(var.getCiName(), CmsCrypto.ENC_VAR_PREFIX + var.getAttribute(VAR_SEC_ATTR_VALUE).getNewValue().substring(CmsCrypto.ENC_PREFIX.length()) + CmsCrypto.ENC_VAR_SUFFIX);
                } else {
                    varsMap.put(var.getCiName(), var.getAttribute(VAR_UNSEC_ATTR_VALUE).getNewValue());
                }
            }
        }
        return varsMap;
    }


    */
/** take a variable name, look it up in the globalVars Map. If the value is a simple value return that. If the value
     * is a reference to a variable in the Cloud Map, look it up in the clodVars Map and return the value *//*

    protected String resolveGlobalVar(Map<String, String> cloudVars, Map<String, String> globalVars, String variableToResolve) {
        //resolving either in the form of - $OO_GLOBAL{xyz} or $OO_GLOBAL{$OO_CLOUD{jkl}}
        //i.e    either a value, or a pointer to Cloud
        if(globalVars==null || variableToResolve==null){
            return null;
        }
        String resolvedValue=globalVars.get(variableToResolve);

        if(resolvedValue!=null && resolvedValue.startsWith(CLOUDVARPFX) && cloudVars!=null){
            resolvedValue=cloudVars.get(stripSymbolics(resolvedValue));
        }

        return resolvedValue;
    }

    */
/** $OO_CLOUD{xyz} returned as xyz *//*

    private String stripSymbolics(String variableReference) {
        return variableReference.substring(variableReference.indexOf("{")+1, variableReference.indexOf("}"));
    }

    private String stripSymbolicsWithPrefix(String variableReference, String prefix) {
        int startIndex = variableReference.indexOf(prefix) + prefix.length();
        return variableReference.substring(startIndex, variableReference.indexOf("}", startIndex));
    }

    */
/** sets the Attributes Dj and Df value, but ensures it is not an unresolved variable reference
     * runtime exceptions stem from here if that is the case*//*

    private String subVarValue(VariableContext variableContext, String attrValue, String resolvedValue, String varName, String replPrefix) {

        String ciName= variableContext.getCiName();

        check4ValidVariable(variableContext, resolvedValue, varName, replPrefix);

        //prefix.$OO_LOCAL{x}.suffix in Dj to-> prefix.RR.suffix
        StringBuilder pattToReplace = new StringBuilder(replPrefix).append(varName).append("\\}");
        String resAfter = attrValue.replaceAll(pattToReplace.toString(), Matcher.quoteReplacement(resolvedValue));
        if(logger.isDebugEnabled()){
            logger.debug("Resolved value set to :"+resAfter+ " in Ci "+ciName);
        }
        return resAfter;
    }

    private void check4ValidVariable(VariableContext variableContext, String resolvedValue, String varName, String replPrefix) {

        long ciId = variableContext.getCiId();
        String ciName= variableContext.getCiName();
        String nsPath= variableContext.getNsPath();
        String attrName = variableContext.getAttrName();

        if (resolvedValue==null ||   		//fix, it is actually okay if resolvedValue equals("")
                resolvedValue.contains(LOCALVARPFX) ||
                resolvedValue.contains(GLOBALVARPFX)||
                resolvedValue.contains(LOCALVARPFX) ) {//substituion did not happen: bad.
            String sb = "error processVars CI-" +
                    ciName + " id-" + ciId +
                    " the attribute- " +
                    attrName +
                    " has a bad " + guessVariableType(replPrefix) + " var reference! value [" + resolvedValue;
            logger.warn(sb);
            throw new CIValidationException(
                    CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
                    getErrorMessage(ciName, nsPath, attrName, resolvedValue, varName, replPrefix));
        }


    }

    protected String getErrorMessage(String ciName, String nsPath, String attrName, String resolvedValue, String varName, String prefix) {
        String attributeDescription = "";
        try {
            attributeDescription = cmProcessor.getAttributeDescription(nsPath, ciName, attrName);
        } catch (Exception ignore) {
            // ignore all errors while retrieving description from meta, it should never fail and affect error message generation.
            // also tests do not inject cmProcessor, so description lookup will throw NPE
        }

        return String.format("%s@%s attribute '%s' [%s] references unknown %s variable '%s'",
                ciName,
                truncateNS(nsPath),
                attributeDescription,
                attrName,
                guessVariableType(prefix),
                varName);
    }

    private String guessVariableType(String prefix) {
        String varType = "local";
        if (prefix!=null){
            switch (prefix) {
                case CLOUDVARRPL:
                    varType = "cloud";
                    break;
                case GLOBALVARRPL:
                    varType = "global";
                    break;
                default:
                    varType = "local";
                    break;
            }
        }
        return varType;
    }

    private String truncateNS(String nsPath) {
        if (nsPath!=null) {
            Matcher matcher = Pattern.compile("(/[^/]+){2}$").matcher(nsPath);
            if (matcher.find()) {
                return matcher.group().substring(1);
            }
        }
        return nsPath;
    }
    */
/** sets the Attributes Dj and Df value, but ensures it is not an unresolved variable reference
     * runtime exceptions stem from here if that is the case*//*

    private String subVarValue(VariableContext variableContext, String attrValue, String resolvedValue, String varName, String replPrefix) {

        String ciName= variableContext.getCiName();

        check4ValidVariable(variableContext, resolvedValue, varName, replPrefix);

        //prefix.$OO_LOCAL{x}.suffix in Dj to-> prefix.RR.suffix
        StringBuilder pattToReplace = new StringBuilder(replPrefix).append(varName).append("\\}");
        String resAfter = attrValue.replaceAll(pattToReplace.toString(), Matcher.quoteReplacement(resolvedValue));
        if(logger.isDebugEnabled()){
            logger.debug("Resolved value set to :"+resAfter+ " in Ci "+ciName);
        }
        return resAfter;
    }

    private String processAllVarsForString(VariableContext variableContext) {
        final String attrValue1 = variableContext.getUnresolvedAttrValue();
        String attrValue = variableContext.getUnresolvedAttrValue();
        boolean inEncrypted = false;
        //check is attribute is used

        if (cmsCrypto.isEncrypted(attrValue)) {
            try {
                attrValue = cmsCrypto.decrypt(attrValue);
            } catch (GeneralSecurityException e) {
                logger.error("Error in getting variable out ");
                throw new CIValidationException(
                        CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
                        getErrorMessage(variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), "", "", ""));
            }
            inEncrypted = true;
        }

        //check if this is an encrypted attribute
        // only all encrypted vars are allowed
        //resolve the value to string put it in value.
        String variableToResolve = "";
        String resolvedValue = "";
        if (attrValue != null) {
            if (attrValue.contains(CLOUDVARPFX)) {
                List<String> varStructures = splitAttrValue(attrValue, CLOUDVARPFX);
                for (String varStructure : varStructures) {
                    variableToResolve = stripSymbolics(varStructure);
                    resolvedValue = variableContext.getCloudVar(variableToResolve);//ez lookup in 1 Cloud Map
                    check4ValidVariable(variableContext, resolvedValue, variableToResolve, CLOUDVARRPL);
                    attrValue = subVarValue(variableContext, attrValue, resolvedValue, variableToResolve, CLOUDVARRPL);
                }
            }

            if (attrValue.contains(GLOBALVARPFX)) {
                List<String> varStructures = splitAttrValue(attrValue, GLOBALVARPFX);
                for (String varStructure : varStructures) {
                    variableToResolve = stripSymbolics(varStructure);
                    //lookup in Global Map; may refer to Cloud in turn but handled there
                    resolvedValue = resolveGlobalVar(variableContext.getCloudVars(), variableContext.getGlobalVars(), variableToResolve);
                    //attrValue = subVarValue(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), attrValue, resolvedValue, variableToResolve, GLOBALVARRPL);
                    check4ValidVariable(variableContext, resolvedValue, variableToResolve, GLOBALVARRPL);
                    attrValue = subVarValue(variableContext, attrValue, resolvedValue, variableToResolve, GLOBALVARRPL);

                }
            }

            if (attrValue.contains(LOCALVARPFX)) {
                List<String> varStructures = splitAttrValue(attrValue, LOCALVARPFX);
                for (String varStructure : varStructures) {
                    variableToResolve = stripSymbolics(varStructure);
                    if (variableContext.getLocalVars() != null) {
                        resolvedValue = variableContext.getLocalVars().get(variableToResolve);
                    } else {
                        resolvedValue = null;
                    }
                    if (resolvedValue != null) {
                        //in case of encrypted
                        while (resolvedValue.contains(CLOUDVARPFX)) {// ez lookup in Cloud Map
                            String varName = stripSymbolicsWithPrefix(resolvedValue, CLOUDVARPFX);
                            String varValue = variableContext.getCloudVars().get(varName);
                            //check4ValidVariable(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), varValue, varName, CLOUDVARRPL);
                            check4ValidVariable(variableContext, varValue, varName, CLOUDVARRPL);
                            resolvedValue = resolvedValue.replaceAll(CLOUDVARRPL + varName + "}", varValue);
                        }

                        while (resolvedValue.contains(GLOBALVARPFX)) {
                            String varName = stripSymbolicsWithPrefix(resolvedValue, GLOBALVARPFX);
                            String varValue = resolveGlobalVar(variableContext.getCloudVars(),
                                    variableContext.getGlobalVars(),
                                    varName); // lookup in Global Map; it may refer to Cloud in turn but handled there
                            //check4ValidVariable(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), varValue, varName, GLOBALVARRPL);
                            check4ValidVariable(variableContext, varValue, varName, GLOBALVARRPL);
                            resolvedValue = resolvedValue.replaceAll(GLOBALVARRPL + varName + "}", varValue);
                        }
                    }
                    //attrValue = subVarValue(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), attrValue, resolvedValue, variableToResolve, LOCALVARRPL);
                    attrValue = subVarValue(variableContext, attrValue, resolvedValue, variableToResolve, LOCALVARRPL);

                }
            }

        }

        //if(true) throw new RuntimeException("hello");
        if (inEncrypted) {
            //is resolved value encrypted , dont encrypt again  .
            if (!cmsCrypto.isVarEncrypted(attrValue)) {
                try {
                    attrValue = cmsCrypto.encrypt(attrValue);
                } catch (GeneralSecurityException | IOException e) {

                }

            }
        }
        return attrValue;

    }


}



*/

/*

private String processAllVarsForString(VariableContext variableContext) {
final String attrValue1 = variableContext.getUnresolvedAttrValue();
        String attrValue = variableContext.getUnresolvedAttrValue();
        boolean inEncrypted = false;
        //check is attribute is used

        if (cmsCrypto.isEncrypted(attrValue)) {
        try {
        attrValue = cmsCrypto.decrypt(attrValue);
        } catch (GeneralSecurityException e) {
        logger.error("Error in getting variable out ");
        throw new CIValidationException(
        CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,
        getErrorMessage(variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), "", "", ""));
        }
        inEncrypted = true;
        }

        //check if this is an encrypted attribute
        // only all encrypted vars are allowed
        //resolve the value to string put it in value.
        String variableToResolve = "";
        String resolvedValue = "";
        if (attrValue != null) {
        if (attrValue.contains(CLOUDVARPFX)) {
        List<String> varStructures = splitAttrValue(attrValue, CLOUDVARPFX);
        for (String varStructure : varStructures) {
        variableToResolve = stripSymbolics(varStructure);
        resolvedValue = variableContext.getCloudVar(variableToResolve);//ez lookup in 1 Cloud Map
        check4ValidVariable(variableContext, resolvedValue, variableToResolve, CLOUDVARRPL);
        attrValue = subVarValue(variableContext, attrValue, resolvedValue, variableToResolve, CLOUDVARRPL);
        }
        }

        if (attrValue.contains(GLOBALVARPFX)) {
        List<String> varStructures = splitAttrValue(attrValue, GLOBALVARPFX);
        for (String varStructure : varStructures) {
        variableToResolve = stripSymbolics(varStructure);
        if (variableContext.getGlobalVars() != null) {
        resolvedValue = variableContext.getGlobalVars().get(variableToResolve);
        } else {
        check4ValidVariable(variableContext, null, variableToResolve, GLOBALVARRPL);

        }
        if (resolvedValue != null) {
        while (resolvedValue.contains(GLOBALVARPFX)) {// ez lookup in Cloud Map
        String varName = stripSymbolicsWithPrefix(resolvedValue, GLOBALVARPFX);
        String varValue = variableContext.getGlobalVars().get(varName);
        check4ValidVariable(variableContext, varValue, varName, GLOBALVARRPL);
        resolvedValue = resolvedValue.replaceAll(GLOBALVARRPL + varName + "}", varValue);
        }
        while (resolvedValue.contains(CLOUDVARPFX)) {// ez lookup in Cloud Map
        String varName = stripSymbolicsWithPrefix(resolvedValue, CLOUDVARPFX);
        String varValue = variableContext.getCloudVars().get(varName);
        //check4ValidVariable(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), varValue, varName, CLOUDVARRPL);
        check4ValidVariable(variableContext, varValue, varName, CLOUDVARRPL);
        resolvedValue = resolvedValue.replaceAll(CLOUDVARRPL + varName + "}", varValue);
        }
        attrValue = subVarValue(variableContext, attrValue, resolvedValue, variableToResolve, GLOBALVARRPL);

        }else {
        check4ValidVariable(variableContext, null, variableToResolve, GLOBALVARRPL);
        }

        }
        }
        if (attrValue.contains(LOCALVARPFX)) {
        List<String> varStructures = splitAttrValue(attrValue, LOCALVARPFX);
        for (String varStructure : varStructures) {
        variableToResolve = stripSymbolics(varStructure);
        if (variableContext.getLocalVars() != null) {
        resolvedValue = variableContext.getLocalVars().get(variableToResolve);
        } else {
        resolvedValue = null;
        }
        if (resolvedValue != null) {
        //in case of encrypted
        while (resolvedValue.contains(CLOUDVARPFX)) {// ez lookup in Cloud Map
        String varName = stripSymbolicsWithPrefix(resolvedValue, CLOUDVARPFX);
        String varValue = variableContext.getCloudVars().get(varName);
        //check4ValidVariable(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), varValue, varName, CLOUDVARRPL);
        check4ValidVariable(variableContext, varValue, varName, CLOUDVARRPL);
        resolvedValue = resolvedValue.replaceAll(CLOUDVARRPL + varName + "}", varValue);
        }

        while (resolvedValue.contains(GLOBALVARPFX)) {
        String varName = stripSymbolicsWithPrefix(resolvedValue, GLOBALVARPFX);
        String varValue = resolveGlobalVar(variableContext.getCloudVars(),
        variableContext.getGlobalVars(),
        varName); // lookup in Global Map; it may refer to Cloud in turn but handled there
        //check4ValidVariable(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), varValue, varName, GLOBALVARRPL);
        check4ValidVariable(variableContext, varValue, varName, GLOBALVARRPL);
        resolvedValue = resolvedValue.replaceAll(GLOBALVARRPL + varName + "}", varValue);
        }
        }
        //attrValue = subVarValue(variableContext.getCiId(), variableContext.getCiName(), variableContext.getNsPath(), variableContext.getAttrName(), attrValue, resolvedValue, variableToResolve, LOCALVARRPL);
        attrValue = subVarValue(variableContext, attrValue, resolvedValue, variableToResolve, LOCALVARRPL);

        }
        }

        }

        //if(true) throw new RuntimeException("hello");
        if (inEncrypted) {
        //is resolved value encrypted , dont encrypt again  .
        if (!cmsCrypto.isVarEncrypted(attrValue)) {
        try {
        attrValue = cmsCrypto.encrypt(attrValue);
        } catch (GeneralSecurityException | IOException e) {

        }

        }
        }
        return attrValue;

        }
*/
