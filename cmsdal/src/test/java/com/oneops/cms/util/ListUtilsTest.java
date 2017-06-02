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
package com.oneops.cms.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.testng.annotations.*;
import org.testng.log4testng.Logger;

import com.oneops.cms.util.ListUtils;

import static org.testng.Assert.*;

/**
 * Unit test
 */
public class ListUtilsTest {
	
	/** the object we will put into the list for the util
	 * to convert. The specialValue field will be the key
	 */
	private class MyBean {

		private String specialValue;
		private int assignedNumber;

		public MyBean(String specialValue) {
			this.specialValue = specialValue;
			this.assignedNumber= new Random().nextInt();
		}

		public String getSpecialValue() {
			return specialValue;
		}

		@Override
		public boolean equals(Object o){
			if (o instanceof MyBean && this.specialValue.equals(((MyBean) o).specialValue)
				                    && this.assignedNumber == (((MyBean) o).assignedNumber)){
				return true;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode(){
			return assignedNumber;
		}
		
	}

	private static final String KEY_FIELD = "specialValue";
	private static final int LIST_SIZE = 5;
	private ListUtils<String> util = new ListUtils<String>();
	private static Logger logger = Logger.getLogger(ListUtilsTest.class);

	@Test
	/**create a list of our MyBean class and validate the util method creates an accurate map
	 where the key is the specialValue and the object is the bean itself*/
	public void testList1() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		List<MyBean> listOfBeans = new ArrayList<MyBean>(LIST_SIZE);
		for (int i = 1; i <= LIST_SIZE; i++) {
			MyBean m = new MyBean(String.valueOf(i));
			listOfBeans.add(m);
		}

		Map<String, MyBean> outMap = util.toMap(listOfBeans, KEY_FIELD);
		assertEquals(LIST_SIZE, outMap.size());
		for(MyBean listMember : listOfBeans){
			assertEquals(listMember, outMap.get(listMember.getSpecialValue()));
		}
		System.out.println(outMap);

	}

}
