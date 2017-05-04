package com.oneops.mybatis;


public class Stats{
    private long noOfCalls = 0;
    private long totalTime = 0;
    private long maxTime = 0;

    public void addTime(long time){
        noOfCalls++;
        totalTime+=time;
        if (maxTime< time){
            maxTime = time;
        }
    }

    public long getNoOfCalls() {
        return noOfCalls;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public Double getAverage(){
        return maxTime==0?0:(double)totalTime/noOfCalls;

    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"noOfCalls\":").append(noOfCalls);
        sb.append(", \"totalTime\":").append(totalTime);
        sb.append(", \"maxTime\":").append(maxTime);
        sb.append(", \"avgTime\" :").append(maxTime==0?0:(double)totalTime/noOfCalls).append("}");
        return sb.toString();
    }
}
