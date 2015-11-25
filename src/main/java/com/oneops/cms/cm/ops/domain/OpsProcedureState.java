package com.oneops.cms.cm.ops.domain;

/**
 * The Enum OpsProcedureState.
 */
public enum OpsProcedureState {
    pending("pending",10),
	active("active",100),
    complete("complete",200),
    failed("failed",300),
    canceled("canceled",400),
    discarded("discarded", 500);

    private String name;
    private int id;

    private OpsProcedureState(String name, int id) {
        this.name = name;
        this.id = id;
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
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }
}
