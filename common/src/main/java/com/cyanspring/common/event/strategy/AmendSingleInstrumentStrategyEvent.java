package com.cyanspring.common.event.strategy;

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AmendSingleInstrumentStrategyEvent extends RemoteAsyncEvent {
	private Map<String, Object> fields;

	public AmendSingleInstrumentStrategyEvent(String key, String receiver,
			Map<String, Object> fields) {
		super(key, receiver);
		this.fields = fields;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

}
