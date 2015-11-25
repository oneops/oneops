package com.oneops.cms.cm.ops.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class OpsProcedureDefinition.
 */
public class OpsProcedureDefinition  implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String name;
	private List<OpsProcedureFlow> flow;
	private String execStrategy;
	private List<OpsFlowAction> actions = new ArrayList<OpsFlowAction>();

	/**
	 * Gets the exec strategy.
	 *
	 * @return the exec strategy
	 */
	public String getExecStrategy() {
		return execStrategy;
	}

	/**
	 * Sets the exec strategy.
	 *
	 * @param execStrategy the new exec strategy
	 */
	public void setExecStrategy(String execStrategy) {
		this.execStrategy = execStrategy;
	}

	/**
	 * Gets the flow.
	 *
	 * @return the flow
	 */
	public List<OpsProcedureFlow> getFlow() {
		return flow;
	}

	/**
	 * Sets the flow.
	 *
	 * @param flow the new flow
	 */
	public void setFlow(List<OpsProcedureFlow> flow) {
		this.flow = flow;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	public List<OpsFlowAction> getActions() {
		return actions;
	}
	
	/**
	 * Sets the actions.
	 *
	 * @param actions the new actions
	 */
	public void setActions(List<OpsFlowAction> actions) {
		this.actions = actions;
	}

}
