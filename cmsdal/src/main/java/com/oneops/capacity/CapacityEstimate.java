/*******************************************************************************
 *
 *   Copyright 2018 Walmart, Inc.
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
package com.oneops.capacity;


import java.util.Map;

public class CapacityEstimate {
    private Map<String, Map<String, Integer>> increase;
    private Map<String, Map<String, Integer>> decrease;
    private String reservationCheck;


    public CapacityEstimate(Map<String, Map<String, Integer>> increase, Map<String, Map<String, Integer>> decrease, String reservationCheck) {
        this.increase = increase;
        this.decrease = decrease;
        this.reservationCheck = reservationCheck;
    }

    public Map<String, Map<String, Integer>> getIncrease() {
        return increase;
    }

    public void setIncrease(Map<String, Map<String, Integer>> increase) {
        this.increase = increase;
    }

    public Map<String, Map<String, Integer>> getDecrease() {
        return decrease;
    }

    public void setDecrease(Map<String, Map<String, Integer>> decrease) {
        this.decrease = decrease;
    }

    public String getReservationCheck() {
        return reservationCheck;
    }

    public void setReservationCheck(String reservationCheck) {
        this.reservationCheck = reservationCheck;
    }

	@Override
	public String toString() {
		return "CapacityEstimate [increase=" + increase + ", decrease=" + decrease + ", reservationCheck="
				+ reservationCheck + "]";
	}
    
    
    
}
