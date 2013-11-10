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
package com.cyanspring.strategy;

import com.cyanspring.common.business.Execution;
import com.cyanspring.common.marketdata.Trade;

public class MarketStatistic {
	private double otVWAP;			// Order timed VWAP
	private double otMarketVol;		// Order timed Market Volume
	private double otMyVol;			// Order timed My Volume
	
	public void tradeUpdate(Trade trade) {
		otVWAP = (otVWAP * otMarketVol + trade.getPrice() * trade.getQuantity()) /
				(otMarketVol + trade.getQuantity());
		otMarketVol += trade.getQuantity();
		
	}
	
	public void executionUpdate(Execution execution) {
		otMyVol += execution.getQuantity();
	}
	
	public void reset() {
		otVWAP = 0;
		otMarketVol = 0;
		otMyVol = 0;
	}

	public double getOtVWAP() {
		return otVWAP;
	}

	public double getOtMarketVol() {
		return otMarketVol;
	}

	public double getOtMyVol() {
		return otMyVol;
	}
	
	@Override
	public String toString() {
		return "[" + 
		"otMyVol: " + otMyVol + ", " + 
		"otMarketVol: " + otMarketVol + ", " + 
		"otVWAP: " + otVWAP + ", " + 
		"]";
	}
}
