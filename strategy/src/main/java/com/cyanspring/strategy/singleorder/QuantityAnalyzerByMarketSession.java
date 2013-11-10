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

public class QuantityAnalyzerByMarketSession extends AbstractQuantityAnalyzer {
	private Map<MarketSessionType, IQuantityAnalyzer> handlerMap;
	public QuantityAnalyzerByMarketSession(Map<MarketSessionType, IQuantityAnalyzer> handlerMap) {
		this.handlerMap = handlerMap;
	}
	
	@Override
	protected QuantityInstruction calculate(SingleOrderStrategy strategy) {
		IQuantityAnalyzer analyzer;
		analyzer = handlerMap.get(strategy.getMarketSession());
		if (analyzer == null)
			analyzer = handlerMap.get(MarketSessionType.DEFAULT);
			
		if (analyzer != null)
			return analyzer.analyze(strategy);
		return null;
	}

}
