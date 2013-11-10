package com.cyanspring.cstw.event;

import java.util.List;
import java.util.Map;

public final class SingleInstrumentStrategySelectionEvent extends
		ObjectSelectionEvent {

	public SingleInstrumentStrategySelectionEvent(Map<String, Object> data,
			List<String> editableFields) {
		super(data, editableFields);
	}

}
