package com.cyanspring.custom.strategy;

import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.strategy.singleorder.AbstractPriceAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class CustomStrategy1PriceAnalyzer extends AbstractPriceAnalyzer {

	@Override
	protected PriceInstruction calculate(QuantityInstruction qtyInstruction,
			SingleOrderStrategy strategy) {
		// TODO implement your strategy logic on pricing here
		return null;
	}

}
