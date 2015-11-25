package com.oneops.cms.dj.domain;

public enum CmsRfcAction {
	add("add",100),
	update("update",200),
	delete("delete",300),
	replace("replace",400);

    private String name;
    private int id;

    private CmsRfcAction(String name, int id) {
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
