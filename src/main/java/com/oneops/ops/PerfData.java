package com.oneops.ops;

public class PerfData {
	private Header header;
	private String[] data;
		
	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public String[] getData() {
		return data;
	}

	public void setData(String[] data) {
		this.data = data;
	}

	public class Header {
		private String ci_id;
		private int step ;
		private long start ;
		private String metric;
		public String getCi_id() {
			return ci_id;
		}
		public void setCi_id(String ci_id) {
			this.ci_id = ci_id;
		}
		public int getStep() {
			return step;
		}
		public void setStep(int step) {
			this.step = step;
		}
		public long getStart() {
			return start;
		}
		public void setStart(long start) {
			this.start = start;
		}
		public String getMetric() {
			return metric;
		}
		public void setMetric(String metric) {
			this.metric = metric;
		}
	}
}
