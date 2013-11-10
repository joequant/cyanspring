package com.cyanspring.cstw.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.OrderField;

public class SingleInstrumentStrategyCache {
	private static final Logger log = LoggerFactory.getLogger(SingleInstrumentStrategyCache.class);
	private Map<String, Instrument> instruments = new HashMap<String, Instrument>();
	private Map<String, Integer> position = new HashMap<String, Integer>();
	private List<Map<String, Object>> innerList = new ArrayList<Map<String, Object>>();
	

	synchronized public void update(Instrument instrument) {
		Instrument existing = instruments.put(instrument.getId(), instrument);
		if (null != existing ) {
			Integer index = position.get(instrument.getId());
			innerList.set(index, instrument.getFields());
		} else {
			position.put(instrument.getId(), position.size());
			innerList.add(instrument.getFields());
		}
	}
	
	synchronized public void clearInstruments(String server) {
		// remove orders from this server from parentOrders
		boolean cleared = false;
		for(Map<String, Object> map: innerList) {
			String name = (String)map.get(OrderField.SERVER_ID.value());
			if(name.equals(server)) {
				String key = (String) map.get(OrderField.ID.value());
				instruments.remove(key);
				log.debug("Removed instrument: " + key);
				cleared = true;
			}
		}
		
		// clear innerOrders then re-set pos in order
		if(cleared) {
			innerList.clear();
			position.clear();
			for(Instrument instrument: instruments.values()) {
				innerList.add(instrument.getFields());
				position.put(instrument.getId(), position.size());
			}
		}
	}
	synchronized public List<Map<String, Object>> getInstruments() {
		return innerList;
	}
	
	synchronized public Instrument getInstrument(String id) {
		return instruments.get(id);
	}
	
	synchronized public void clear() {
		instruments.clear();
		innerList.clear();
	}

}
