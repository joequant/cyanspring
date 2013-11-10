package com.cyanspring.cstw.event;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.event.AsyncEvent;

public class GuiSingleInstrumentStrategyUpdateEvent extends AsyncEvent {
	Instrument instrument;

	public GuiSingleInstrumentStrategyUpdateEvent(Instrument instrument) {
		super();
		this.instrument = instrument;
	}

	public Instrument getInstrument() {
		return instrument;
	}
	
}
