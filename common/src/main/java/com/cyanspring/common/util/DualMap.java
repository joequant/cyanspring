/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
  * The dual map allows key/value to retrieve value/key easily with the enforcement of
  * BOT KEY AND VALUE ARE UNIQUE
 * @param <T> key
 * @param <U> value
 */
public class DualMap<T, U> {
	private static final long serialVersionUID = -8696599939211671404L;
	HashMap<T, U> map1 = new HashMap<T, U>();
	HashMap<U, T> map2 = new HashMap<U, T>();
	
	
	public DualMap() {
		super();
	}
	
	public DualMap(Map<? extends T, ? extends U> map) {
		putAll(map);
	}
	
	synchronized public U put(T t, U u) {
		// first check whether value already exists
		T t2 = map2.get(u);
		if (t2 != null) 
			map1.remove(t2);
		
		// then checck whether key already exists for map2
		U u2 = map1.get(t);
		if (u2 != null) 
			map2.remove(u2);
		
		U result = map1.put(t, u);
		map2.put(u, t);
		return result; 
	}

	synchronized public U remove(T t) {
		// first check whether value already exists
		U u = map1.remove(t);
		
		if(u != null) {
			map2.remove(u);
		}
		
		return u;
	}

	synchronized public T removeKeyByValue(U u) {
		// first check whether value already exists
		T t = map2.remove(u);
		
		if(t != null) {
			map1.remove(t);
		}
		
		return t;
	}

	synchronized public U get(T t) {
		return map1.get(t);
	}

	synchronized public T getKeyByValue(U u) {
		return map2.get(u);
	}
	
	synchronized public boolean containsKey(T t) {
		return map1.containsKey(t);
	}
	
	synchronized public boolean containsValue(U u) {
		return map1.containsValue(u);
	}
	
	synchronized public Set<Entry<T, U>> entrySet() {
		return map1.entrySet();
	}
	
	synchronized public Collection<U> values() {
		return map1.values();
	}

	synchronized public Set<T> keySet() {
		return map1.keySet();
	}

	synchronized public void putAll(Map<? extends T, ? extends U> map) {
 		for(java.util.Map.Entry<? extends T, ? extends U> entry: map.entrySet()) {
 			put(entry.getKey(), entry.getValue());
 		}
	}
	
	synchronized public void clear() {
		map1.clear();
		map2.clear();
	}
}
