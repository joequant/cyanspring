package com.cyanspring.common.event.strategy;

public class NewSingleInstrumentStrategyReplyEvent extends
		StrategyChangeReplyEvent {

	public NewSingleInstrumentStrategyReplyEvent(String key, String receiver,
			String txId, boolean success, String message) {
		super(key, receiver, txId, success, message);
	}

}
