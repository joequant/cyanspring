package com.cyanspring.common.event.strategy;

import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.event.RemoteAsyncEvent;

public final class MultiInstrumentStrategyFieldDefUpdateEvent extends
		RemoteAsyncEvent {
	private MultiInstrumentStrategyDisplayConfig config;
	public MultiInstrumentStrategyFieldDefUpdateEvent(String key,
			String receiver,
			MultiInstrumentStrategyDisplayConfig config) {
		super(key, receiver);
		this.config = config;
	}
	public MultiInstrumentStrategyDisplayConfig getConfig() {
		return config;
	}
	
}
