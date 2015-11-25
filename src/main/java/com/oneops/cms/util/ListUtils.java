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
