package com.cyanspring.common.event.strategy;

import java.util.Map;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class SingleInstrumentStrategyFieldDefUpdateEvent extends
		RemoteAsyncEvent {
	private String name;
	private Map<String, FieldDef> fieldDefs;
	
	public SingleInstrumentStrategyFieldDefUpdateEvent(String key,
			String receiver, String name, Map<String, FieldDef> fieldDefs) {
		super(key, receiver);
		this.name = name;
		this.fieldDefs = fieldDefs;
	}

	public String getName() {
		return name;
	}

	public Map<String, FieldDef> getFieldDefs() {
		return fieldDefs;
	}

}
