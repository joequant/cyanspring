package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ClearMultiAlertEvent extends RemoteAsyncEvent {
	private String instrKey;

	public ClearMultiAlertEvent(String key, String instrKey, String receiver) {
		super(key, receiver);
		this.instrKey = instrKey;
	}

	public String getInstrKey() {
		return instrKey;
	}
}
