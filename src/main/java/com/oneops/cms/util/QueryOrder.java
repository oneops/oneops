package com.oneops.cms.util;

public enum QueryOrder {

	DESC ("desc", "<", ">"), ASC("asc", ">", "<");
	
	QueryOrder(String sort, String aboveOperator, String belowOperator) {
		this.sort = sort;
		this.aboveOperator = aboveOperator;
		this.belowOperator = belowOperator;
	}
	
	private String sort;
	private String aboveOperator;
	private String belowOperator;
	
	public static QueryOrder queryOrder(String text) {
		QueryOrder order = DESC;
		if (text != null && text.toLowerCase().startsWith("asc")) {
			order = ASC;
		}
		return order;
	}

	public String getSort() {
		return sort;
	}

	public String getAboveOperator() {
		return aboveOperator;
	}

	public String getBelowOperator() {
		return belowOperator;
	}
	
}
