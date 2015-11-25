package com.oneops.search.domain;

import org.springframework.data.elasticsearch.annotations.Document;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;

/**
 * @author ranand
 *
 */
@Document(indexName = "cms")
public class CmsCISearch extends CmsCISimple {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private NotificationMessage ops;
	
	private CmsWorkOrderSimple workorder;
	
	public NotificationMessage getOps() {
		return ops;
	}

	public void setOps(NotificationMessage ops) {
		this.ops = ops;
	}

	public CmsWorkOrderSimple getWorkorder() {
		return workorder;
	}

	public void setWorkorder(CmsWorkOrderSimple workorder) {
		this.workorder = workorder;
	}

}
