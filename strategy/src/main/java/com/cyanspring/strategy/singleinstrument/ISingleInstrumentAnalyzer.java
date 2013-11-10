package com.cyanspring.strategy.singleinstrument;

import com.cyanspring.common.strategy.PriceInstruction;

public interface ISingleInstrumentAnalyzer {
	PriceInstruction analyze(SingleInstrumentStrategy strategy);
}
