package com.oneops.ops.states;

public enum CiOpsState {
    notify("notify",10),
    overutilized("overutilized",25),
    underutilized("underutilized",20),
    unhealthy("unhealthy",30),
    bad("bad",40);

    private String name;
    private int weight;

    private CiOpsState(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

}
