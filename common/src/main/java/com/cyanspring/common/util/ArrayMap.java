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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Dennis Chen
 * ArrayMap implements both the functions of array and hash map
 * @param <T> key type
 * @param <U> value type
 */
public class ArrayMap<T, U> {
	protected HashMap<T, U> map = new HashMap<T, U>();
	protected HashMap<T, Integer> index = new HashMap<T, Integer>();
	protected ArrayList<U> array = new ArrayList<U>();
	
	synchronized public U put(T t, U u) {
		U result = map.put(t, u);
		if(null != result) {
			Integer pos = index.get(t);
			array.set(pos, u);
		} else {
			Integer pos = array.size();
			array.add(u);
			index.put(t, pos);
		}
		return result;
	}
	
	// removing is slow for this data structure
	// we speed up sequential access at the cost of
	// removal operation
	synchronized public U remove(T t) {
		U result = map.remove(t);
		if(null != result) {
			Integer pos = index.remove(t);
			array.remove(pos.intValue());
			// need to rebuild the index map
			for(T key: map.keySet()) {
				Integer i = index.get(key);
				if(i>pos) {
					index.put(key, i-1);
				}
			}
		}
		return result;
	}
	
	synchronized public int indexOf(T t) {
		return index.get(t);
	}
	
	synchronized public U get(T t) {
		return map.get(t);
	}
	
	synchronized public U get(int i) {
		return array.get(i);
	}
	
	synchronized public ArrayList<U> toArray() {
		return array;
	}
	
	synchronized public HashMap<T, U> toMap() {
		return map;
	}

	synchronized public int size() {
		return array.size();
	}
	
	synchronized public void clear() {
		map.clear();
		array.clear();
		index.clear();
	}
}
