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
package com.cyanspring.sample.multiinstrument.lowhigh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;

public class LowHighStrategy extends MultiInstrumentStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(LowHighStrategy.class);
	
	@Override
	protected void checkCompulsoryStrategyFields(Map<String, Object> strategyLevelParams, 
			List<Map<String, Object>> instrumentLevelParams) throws StrategyException {
		super.checkCompulsoryStrategyFields(strategyLevelParams, instrumentLevelParams);

		for(Map<String, Object> instrParams: instrumentLevelParams) {
			Object objShort = instrParams.get("Shortable");
			if(null != objShort) {
				if(null == instrParams.get("High stop") ||
				   null == instrParams.get("High take") ||
				   null == instrParams.get("High flat"))
					throw new StrategyException("'High stop', 'High take', 'High flat' can not be empty if 'Shortable' is specified");
			}
		}
	}

	@Override
	protected PriceInstruction analyze() {
		List<PriceAllocation> pas = new ArrayList<PriceAllocation>();
		for(Instrument instr: data.getInstrumentData().values()) {
			String symbol = instr.getSymbol();
			Quote quote = getQuotes().get(symbol);
			double lowFlat = instr.get(double.class, "Low flat");
			double lowTake = instr.get(double.class, "Low take");
			double lowStop = instr.get(double.class, "Low stop");
			double position = instr.getPosition();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			if(PriceUtils.GreaterThan(position, 0)){ 
				if(!PriceUtils.isZero(quote.getLast()) && PriceUtils.EqualLessThan(quote.getLast(), lowStop)) {
					logInfo(">>>>>> Sending Low Stop @" + quote.getLast() + " <<<<<<");
					PriceAllocation pa = flatInstrumentPosition(instr);
					pas.add(pa);
				} else {
					pas.add(new PriceAllocation(symbol, OrderSide.Sell, lowFlat, position, ExchangeOrderType.LIMIT, instr.getId()));
				}
			} 
			
			if (PriceUtils.EqualGreaterThan(position, 0) && PriceUtils.LessThan(position, qty)) {
				double delta = qty - position;
				pas.add(new PriceAllocation(symbol, OrderSide.Buy, lowTake, delta, ExchangeOrderType.LIMIT, instr.getId()));
			}
					
			if(instr.fieldExists("Shortable")) {
				double highStop = instr.get(double.class, "High stop");
				double highTake = instr.get(double.class, "High take");
				double highFlat = instr.get(double.class, "High flat");
				
				if(PriceUtils.LessThan(position, 0)) {
					if(!PriceUtils.isZero(quote.getLast()) && PriceUtils.EqualGreaterThan(quote.getLast(), highStop)){ //stop lost
						logInfo(">>>>>> High Stop @" + quote.getLast() + " <<<<<<");
						PriceAllocation pa = flatInstrumentPosition(instr);
						pas.add(pa);
					} else {
						pas.add(new PriceAllocation(symbol, OrderSide.Buy, highFlat, -position, ExchangeOrderType.LIMIT, instr.getId()));
					}
				} 
				
				if (PriceUtils.EqualLessThan(position, 0) && PriceUtils.LessThan(Math.abs(position), qty)) { 
					double delta = qty + position;
					pas.add(new PriceAllocation(symbol, OrderSide.Sell, highTake, delta, ExchangeOrderType.LIMIT, instr.getId()));
				}
			}
		}
		if(pas.size()>0) {
			PriceInstruction pi = new PriceInstruction();
			for(PriceAllocation pa: pas) {
				pi.add(pa);
			}
			return pi;
		}
		return null;
	}

	
}
