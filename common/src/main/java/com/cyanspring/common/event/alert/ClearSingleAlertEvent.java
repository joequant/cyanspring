package com.cyanspring.common.event.alert;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class ClearSingleAlertEvent extends RemoteAsyncEvent {

	public ClearSingleAlertEvent(String key, String receiver) {
		super(key, receiver);
	}

}
