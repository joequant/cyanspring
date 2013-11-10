package com.cyanspring.sample.custom;

import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.strategy.singleorder.AbstractPriceAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class DummyPriceAnalyzer extends AbstractPriceAnalyzer {

	@Override
	protected PriceInstruction calculate(QuantityInstruction qtyInstruction,
			SingleOrderStrategy strategy) {
		// TODO Auto-generated method stub
		return null;
	}

}
