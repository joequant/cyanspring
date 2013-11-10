package com.cyanspring.common.event.strategy;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class SingleInstrumentStrategyUpdateEvent extends RemoteAsyncEvent {
	protected Instrument instrument;

	public SingleInstrumentStrategyUpdateEvent(String key, String receiver,
			Instrument instrument) {
		super(key, receiver);
		this.instrument = instrument;
	}

	public Instrument getInstrument() {
		return instrument;
	}
	
}
