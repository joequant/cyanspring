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
package com.cyanspring.common.marketsession;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * This class defines all valid exchange session types and provide a method to retrieve valid exchange
 * sessions for a particular exchange
 */

public enum MarketSessionType {
	// add market sessions for other exchanges here
	DEFAULT, // not a real market session but used by strategy framework for default handling
	AM_NO_TRADING,
	AM_OPEN_AUCTION,
	AM_OPEN_AUCTION_MARKET_TYPE_ONLY, 
	AM_OPEN_AUCTION_BLOCKING,
	AM_CONTINUOUS_TRADING,
	LUNCH_BLOCKING, 				
	LUNCH_BLOCKING_AMEND_CANCEL_ALLOW, 
	PM_CONTINUOUS_TRADING,
	PM_NO_TRADING;
	
	
	// add exchange to market session set here
	private static Map<ExchangeType, Set<MarketSessionType>> map = new HashMap<ExchangeType, Set<MarketSessionType>>();
	static {
		Set<MarketSessionType> types = Collections.unmodifiableSet(EnumSet.of(
				AM_NO_TRADING,
				AM_OPEN_AUCTION,
				AM_OPEN_AUCTION_MARKET_TYPE_ONLY, 
				AM_OPEN_AUCTION_BLOCKING,
				AM_CONTINUOUS_TRADING,
				LUNCH_BLOCKING, 				
				LUNCH_BLOCKING_AMEND_CANCEL_ALLOW, 
				PM_CONTINUOUS_TRADING,
				PM_NO_TRADING));
		
		map.put(ExchangeType.HKEX, types);
	}

	public Set<MarketSessionType> getMarketSessionTypes(ExchangeType exchangeType) {
		return map.get(exchangeType);
	}
}
