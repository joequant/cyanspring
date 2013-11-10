package com.cyanspring.custom.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;


public class CustomStrategy2 extends MultiInstrumentStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(CustomStrategy2.class);
	
	@Override
	protected PriceInstruction analyze() {
		return null;
	}


}
