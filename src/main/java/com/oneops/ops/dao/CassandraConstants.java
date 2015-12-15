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
package com.oneops.ops.dao;

public interface CassandraConstants {
	// keys used to set the cassandra hashes
	static final String UPDATED = "updated";
	static final String STEP = "step";
	static final String TYPE = "type";
	static final String MIN = "min";
	static final String MAX = "max";
	static final String AVERAGE = "average";
	static final String COUNT = "count";
	static final String SUM = "sum";
	static final String CHART = "chart";
	static final String SERIES = "series";
	static final String NAME = "name";
	static final String LOGBUCKET = "average-5m";	

}
