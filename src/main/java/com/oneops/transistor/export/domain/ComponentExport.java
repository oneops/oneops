package com.oneops.transistor.export.domain;

import java.util.ArrayList;
import java.util.List;

public class ComponentExport extends ExportCi{
	private List<ExportCi> attachments;
	private List<ExportCi> monitors;
	private List<String> depends;
	private String template;

	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public List<ExportCi> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<ExportCi> attachments) {
		this.attachments = attachments;
	}
	public void addAttachment(ExportCi attachment) {
		if (this.attachments == null) {
			this.attachments = new ArrayList<ExportCi>();
		}
		this.attachments.add(attachment);
	}
	
	public List<String> getDepends() {
		return depends;
	}
	public void setDepends(List<String> dependsOns) {
		this.depends = dependsOns;
	}
	public void addDepends(String dependsOn) {
		if (this.depends == null) {
			this.depends = new ArrayList<String>();
		}
		this.depends.add(dependsOn);
	}
	public void addMonitor(ExportCi monitor) {
		if (this.monitors == null) {
			this.monitors = new ArrayList<ExportCi>();
		}
		this.monitors.add(monitor);
	}
	public List<ExportCi> getMonitors() {
		return monitors;
	}
	
}
