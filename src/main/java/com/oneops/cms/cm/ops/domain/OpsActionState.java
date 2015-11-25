package com.oneops.cms.cm.ops.domain;

/**
 * The Enum OpsActionState.
 */
public enum OpsActionState {
    pending("pending",10),
    inprogress("inprogress",100),
    complete("complete",200),
    failed("failed",300),
    canceled("canceled",400);

    private String name;
    private int id;

    private OpsActionState(String name, int id) {
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
