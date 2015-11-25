package com.oneops.ops.states;

import java.util.List;

public class CiOpsStateMachine {
	
	public static String getCiState(List<String> eventStates) {
		
		String state = "good";
		int weight = 0;
		
		for (String eventState : eventStates) {
			int eventWeight = CiOpsState.valueOf(eventState).getWeight();
			if (eventWeight > weight) {
				state = eventState;
				weight = eventWeight;
			}
		}
		return state; 
	}

}
