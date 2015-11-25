package com.oneops.opamp.cache;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheLoader;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;

/**
 * 
 * @author glall
 * Loads the 'WatchedBy' aka 'Monitor'  relation attributes; key {manifestId:toCiName:attributeName}
 */
@Component
public class WatchedByAttributeCacheLoader extends CacheLoader<String, String> {
	/**
	 * Logger instance
	 */
	private static Logger logger = Logger.getLogger(WatchedByAttributeCacheLoader.class);

	/**
	 * Cms CI processor
	 */
	@Autowired
	private CmsCmProcessor cmProcessor;

	@Override
	public String load(String key) {
		logger.warn("Loading Watched By Attributes from cms for " + key);
		String attributeValue = null;
		if (key == null) {
			throw new IllegalArgumentException("key can not be null");
		} else {
			//split the key
			String[] parts = key.split(":");
			long manifestId = Long.valueOf(parts[0]);
			String source = parts[1];
			String attributeName = parts[2];
			attributeValue = getAttributeValue(manifestId, source, attributeName);
		}
		logger.info("loaded  the attribute for key: " + key + " with value " + attributeValue);

		return attributeValue;
	}

	private String getAttributeValue(Long manifestId, String source, String attributeName) {
		List<CmsCIRelation> manifestRelations = cmProcessor.getFromCIRelationsByClassAndCiName(manifestId, "manifest.WatchedBy", null, "manifest.Monitor", source);
		String attributeValue = null;
		if (manifestRelations.size() > 0) {
			CmsCIRelationAttribute attribute = manifestRelations.get(0).getAttribute(attributeName);
			
			attributeValue = (attribute!=null)?attribute.getDfValue(): StringUtils.EMPTY;
			if(attribute==null) {
				logger.warn("The relation attribute :"+attributeName+" could not be found for manifestId "+manifestId+" source "+source);
			}
		}
		return attributeValue;
	}

}

