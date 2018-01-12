package com.oneops;

import java.io.Serializable;

public abstract class OneopsObject implements Serializable {
    String name;
    String path;
    long id;
    long nsId;

    public long getNsId() {
        return nsId;
    }

    public void setNsId(long nsId) {
        this.nsId = nsId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString() {
        return getString();
    }

    protected String getString() {
        return "ID: " + id + " Name: [" + name + "]";
    }
}
