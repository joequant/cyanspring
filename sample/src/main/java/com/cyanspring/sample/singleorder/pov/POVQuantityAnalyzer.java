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
package com.cyanspring.sample.singleorder.pov;

import java.text.DecimalFormat;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.MarketStatistic;
import com.cyanspring.strategy.singleorder.AbstractQuantityAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class POVQuantityAnalyzer extends AbstractQuantityAnalyzer {

	@Override
	public QuantityInstruction calculate(SingleOrderStrategy strategy) {
		ParentOrder parentOrder = strategy.getParentOrder();
		Quote quote = strategy.getAdjQuote();
		double pov = parentOrder.get(Double.TYPE, OrderField.POV.value()) / 100;
		MarketStatistic marketStatistic = strategy.getMarketStatistic();
		double runningPOV = 0;
		if(!PriceUtils.Equal(marketStatistic.getOtMarketVol(), 0))
			runningPOV = marketStatistic.getOtMyVol() * 100 / marketStatistic.getOtMarketVol();
		strategy.logInfo("MktStat: " + marketStatistic);
		strategy.logInfo("Running POV: " + 
				new DecimalFormat("#0.00").format(runningPOV));

		QuantityInstruction qi = new QuantityInstruction();
		
		double aggressiveQty = (pov * marketStatistic.getOtMarketVol() - marketStatistic.getOtMyVol()) / (1 - pov);
		
		double base = parentOrder.getSide().isBuy()? quote.getBidVol():quote.getAskVol();
		double passiveQty = pov * base /(1-pov);
		
		//if we over executed our pov
		if(PriceUtils.LessThan(aggressiveQty, 0)) {
			passiveQty += aggressiveQty;
			aggressiveQty = 0;
		}
		
		qi.setAggresiveQty(aggressiveQty);
		qi.setPassiveQty(passiveQty);
		return qi;
	}
}
