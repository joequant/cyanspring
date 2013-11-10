/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.strategy.singleorder;

import java.util.Map;

import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.strategy.PriceInstruction;

public class PriceAnalyzerByMarketSession extends AbstractPriceAnalyzer {
	
	private Map<MarketSessionType, IPriceAnalyzer> handlerMap;
	public PriceAnalyzerByMarketSession(Map<MarketSessionType, IPriceAnalyzer> handlerMap) {
		this.handlerMap = handlerMap;
	}
	
	@Override
	protected PriceInstruction calculate(QuantityInstruction qtyInstruction, 
			SingleOrderStrategy strategy) {
		IPriceAnalyzer analyzer;
		analyzer = handlerMap.get(strategy.getMarketSession());
		if (analyzer == null)
			analyzer = handlerMap.get(MarketSessionType.DEFAULT);
			
		if (analyzer != null)
			return analyzer.analyze(qtyInstruction, strategy);
		return null;
	}
}
