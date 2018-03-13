package com.oneops.crawler;

import java.io.Serializable;

public class ESRecord implements Serializable {
    String id;
    Object source;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }
}
