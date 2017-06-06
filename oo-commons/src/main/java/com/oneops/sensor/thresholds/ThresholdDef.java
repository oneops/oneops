/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.sensor.thresholds;

/**
 * Threshold data
 */
public class ThresholdDef {
    private String name;
    private String bucket;
    private String metric;
    private String stat;
    private String state;
    private String cooloff;
    private StmtParams trigger;
    private StmtParams reset;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public StmtParams getTrigger() {
        return trigger;
    }

    public void setTrigger(StmtParams trigger) {
        this.trigger = trigger;
    }

    public StmtParams getReset() {
        return reset;
    }

    public void setReset(StmtParams reset) {
        this.reset = reset;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public String getCooloff() {
        return cooloff;
    }

    public void setCooloff(String cooloff) {
        this.cooloff = cooloff;
    }

    /**
     * Esper statement params
     */
    public class StmtParams {
        private int numocc;
        private int duration;
        private double value;
        private String operator;

        public int getNumocc() {
            return numocc;
        }

        public void setNumocc(int numocc) {
            this.numocc = numocc;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
    }

}
