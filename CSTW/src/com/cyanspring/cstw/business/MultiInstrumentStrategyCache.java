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
package com.cyanspring.cstw.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.OrderField;

public class MultiInstrumentStrategyCache {
	private Map<String, Map<String, Object>> strategyCache = new HashMap<String, Map<String, Object>>();
	private Map<String, Integer> keyIndex = new HashMap<String, Integer>();
	private List<Map<String, Object>> strategyList = new ArrayList<Map<String, Object>>();
	private Map<String, List<Map<String, Object>>> instrumentCache = new HashMap<String, List<Map<String,Object>>>();

	public void  update(MultiInstrumentStrategyData data) {
		String id = data.getId();
//		String server = data.get(String.class, OrderField.SERVER.value());
//		data.put(OrderField.SERVER.value(), server);
		Map<String, Object> existing = strategyCache.put(id, data.getFields());
		if (null != existing ) {
			Integer index = keyIndex.get(id);
			strategyList.set(index.intValue(), data.getFields());
		} else {
			int index = strategyList.size();
			keyIndex.put(id, index);
			strategyList.add(data.getFields());
		}

		ArrayList<Map<String, Object>> instruments = new ArrayList<Map<String,Object>>();
		for(Instrument instr: data.getInstrumentData().values()) {
//			instr.put(OrderField.SERVER.value(), server);
			instruments.add(instr.getFields());
		}
		instrumentCache.put(data.getId(), instruments);
	}
	
	public void clear(String server) {
		boolean cleared = false;
		for(Map<String, Object> map: strategyList) {
			if(map.get(OrderField.SERVER_ID.value()).equals(server)) {
				String id = (String)map.get(OrderField.ID.value());
				strategyCache.remove(id);
				instrumentCache.remove(id);
				cleared = true;
			}
		}
		
		// rebuild keyIndex map and list
		if(cleared) {
			keyIndex.clear();
			strategyList.clear();
			for(Entry<String, Map<String, Object>> entry: strategyCache.entrySet()) {
				int index = strategyList.size();
				strategyList.add(entry.getValue());
				keyIndex.put(entry.getKey(), index);
			}
		}
	}
	
	public Map<String, Object> getStrategy(String id) {
		return strategyCache.get(id);
	}
	public List<Map<String, Object>> getStrategyList() {
		return strategyList;
	}
	
	public List<Map<String, Object>> getInstrumentList(String id) {
		return instrumentCache.get(id);
	}
}
