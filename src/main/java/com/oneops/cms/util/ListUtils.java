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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ListUtils.
 *
 * @param <K> the key type
 */
public class ListUtils<K> {
    
	private static String camelHump(String str) {
        if(str != null && (str = str.trim()).length() > 0) {
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        return str;
    }
 
    private static String convertFieldToAccessor(String filed) {
        return "get" + camelHump(filed);
    }
 
    /**
     * To map.
     *
     * @param <V> the value type
     * @param list the list
     * @param keyField the key field
     * @return the map
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    public <V> Map<K, V> toMap(List<V> list, String keyField) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String accessor = convertFieldToAccessor(keyField);
        Map<K, V> map = new HashMap<K, V>();
        for(V obj : list) {
            Method method = obj.getClass().getMethod(accessor);
            @SuppressWarnings("unchecked")
			K key = (K)method.invoke(obj);
            map.put(key, obj);
        }
        return map;
    }

    /**
     * To map.
     *
     * @param <V> the value type
     * @param list the list
     * @param keyField the key field
     * @return the map
     * @throws NoSuchMethodException the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    public <V> Map<K, List<V>> toMapOfList(List<V> list, String keyField) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String accessor = convertFieldToAccessor(keyField);
        Map<K, List<V>> map = new HashMap<K, List<V>>();
        for(V obj : list) {
            Method method = obj.getClass().getMethod(accessor);
            @SuppressWarnings("unchecked")
			K key = (K)method.invoke(obj);
            if (!map.containsKey(key)) {
            	map.put(key, new ArrayList<V>());
            }
            map.get(key).add(obj);
        }
        return map;
    }
}
