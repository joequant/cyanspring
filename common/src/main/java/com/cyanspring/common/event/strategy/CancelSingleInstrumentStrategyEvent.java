package com.cyanspring.common.event.strategy;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class CancelSingleInstrumentStrategyEvent extends RemoteAsyncEvent {
	private String txId;
	
	public CancelSingleInstrumentStrategyEvent(String key, String receiver,
			String txId) {
		super(key, receiver);
		this.txId = txId;
	}
	public String getTxId() {
		return txId;
	}

}
