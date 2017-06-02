package com.oneops.controller.cms;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import org.apache.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * File created by oleg on 9/28/16.
 */
public class OfferingsMatcher {
    private static Logger logger = Logger.getLogger(OfferingsMatcher.class);
    private CmsCmProcessor cmsCmProcessor;
    private ExpressionParser exprParser;

    public static String convert(String elasticExp) {
        return elasticExp.replace(":", "=='").replace("*.[1 TO *]", "[a-zA-Z0-9.]*").replace(".size", "['size']").replaceFirst("ciClassName==", "ciClassName matches ").replace(".Compute", ".Compute'").replace(".*Compute", ".*Compute'")+"'";
    }

    public static boolean isLikelyElasticExpression(String elasticExp) {
        return elasticExp.contains(":") || elasticExp.contains("ciAttribute.size");
    }

    public void setCmsCmProcessor(CmsCmProcessor cmsCmProcessor) {
        this.cmsCmProcessor = cmsCmProcessor;
    }

    public void setExprParser(ExpressionParser exprParser) {
        this.exprParser = exprParser;
    }

    List<CmsCI> getEligbleOfferings(CmsRfcCISimple cmsRfcCISimple, String offeringNS) {
        List<CmsCI> offerings = new ArrayList<>(); 
        List<CmsCI> list = cmsCmProcessor.getCiBy3(offeringNS, "cloud.Offering", null);
        for (CmsCI ci: list){
            CmsCIAttribute criteriaAttribute = ci.getAttribute("criteria");
            String criteria = criteriaAttribute.getDfValue();
            if (isLikelyElasticExpression(criteria)){
                logger.warn("cloud.Offering CI ID:"+ci.getCiId()+" likely still has elastic search criteria. Evaluation may not be successful!");
                logger.info("ES criteria:"+criteria);
                criteria = convert(criteria);
                logger.info("Converted SPEL criteria:"+criteria);
            }
            Expression expression = exprParser.parseExpression(criteria);
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setRootObject(cmsRfcCISimple);
            boolean match = (boolean) expression.getValue(context, Boolean.class);
            if (match){
                offerings.add(ci);
            }
        }
        return offerings;
    }

    public static void main(String[] args) {
        String elasticExp= "(ciClassName:bom.*.[1 TO *].Compute OR ciClassName:bom.Compute) AND ciAttributes.size:M";
        if (isLikelyElasticExpression(elasticExp)){
            System.out.println(convert(elasticExp));
        }
        System.out.print(Pattern.compile("bom.([a-zA-Z0-9.]+.)*Compute").matcher("bom.test.21.45.Compute").matches());
    }

}                               
