package com.cyanspring.common.event.strategy;

public class AmendSingleInstrumentStrategyReplyEvent extends
		StrategyChangeReplyEvent {

	public AmendSingleInstrumentStrategyReplyEvent(String key, String receiver,
			String txId, boolean success, String message) {
		super(key, receiver, txId, success, message);
	}

}
