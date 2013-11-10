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
package com.cyanspring.sample.multiinstrument.dollarneutral;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.multiinstrument.IMultiInstrumentAnalyzer;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;
import com.cyanspring.strategy.utils.QuoteUtil;

public class DollarNeutralAnalyzer implements IMultiInstrumentAnalyzer {
	@Override
	public PriceInstruction analyze(MultiInstrumentStrategy strategy) {
		double short1Long2Ratio = calShort1Long2(strategy);
		double long1Short2Ratio = calLong1Short2(strategy);
		strategy.logDebug("short1Long2Ratio: " + short1Long2Ratio);
		strategy.logDebug("long1Short2Ratio: " + long1Short2Ratio);
		if(strategy.isZeroPosition())
			return workZeroPosition(strategy, short1Long2Ratio, long1Short2Ratio);
		else if(isLegged(strategy)) {
			return workLeggedPosition(strategy);
		} else {
			return workTakenPosition(strategy, short1Long2Ratio, long1Short2Ratio);
		}
	}
	
	private PriceInstruction workTakenPosition(MultiInstrumentStrategy strategy, double short1Long2Ratio, double long1Short2Ratio) {
		boolean isShort1Long2 = isShort1Long2(strategy);
		if(isShort1Long2 && !PriceUtils.isZero(long1Short2Ratio) && long1Short2Ratio > strategy.getData().get(double.class, "High stop")) { //stop loss
			strategy.logInfo("short1Long2Ratio: " + short1Long2Ratio);
			strategy.logInfo("long1Short2Ratio: " + long1Short2Ratio);
			strategy.logInfo(">>>>>>>> High Stop Opportunity <<<<<<<<<<");
			return strategy.flatPostionInstructions();
		} else if (!isShort1Long2 && !PriceUtils.isZero(short1Long2Ratio) && short1Long2Ratio < strategy.getData().get(double.class, "Low stop")) { //stop loss
			strategy.logInfo("short1Long2Ratio: " + short1Long2Ratio);
			strategy.logInfo("long1Short2Ratio: " + long1Short2Ratio);
			strategy.logInfo(">>>>>>>> Low Stop Opportunity <<<<<<<<<<");
			return strategy.flatPostionInstructions();
		} else if(isShort1Long2 && !PriceUtils.isZero(long1Short2Ratio) && long1Short2Ratio < strategy.getData().get(double.class, "High flat")) {
			strategy.logInfo("short1Long2Ratio: " + short1Long2Ratio);
			strategy.logInfo("long1Short2Ratio: " + long1Short2Ratio);
			strategy.logInfo(">>>>>>>> High Flat Opportunity <<<<<<<<<<");
			return strategy.flatPostionInstructions();
		} else if (!isShort1Long2 && !PriceUtils.isZero(short1Long2Ratio) && short1Long2Ratio > strategy.getData().get(double.class, "Low flat")) {
			strategy.logInfo("short1Long2Ratio: " + short1Long2Ratio);
			strategy.logInfo("long1Short2Ratio: " + long1Short2Ratio);
			strategy.logInfo(">>>>>>>> Low Flat Opportunity <<<<<<<<<<");
			return strategy.flatPostionInstructions();
		}
		return null;
	}
	
	private boolean isShort1Long2(MultiInstrumentStrategy strategy) {
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			if(instr.get(int.class, "Leg") == 1) {
				double position = instr.getPosition();
				if(PriceUtils.LessThan(position, 0))
					return true;
				if(PriceUtils.GreaterThan(position, 0))
					return false;
			}
		}
		return false;
	}

	protected boolean isLegged(MultiInstrumentStrategy strategy) {
		boolean legged = false;
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			double position = instr.getPosition();
			double refQty = instr.get(double.class, "Ref qty");
			if(!PriceUtils.Equal(Math.abs(position), refQty)) {
				strategy.logWarn("We have got legged " + instr.getSymbol() + ": " + refQty + ", " + position);
				legged = true;
			}
		}
		
		if(legged)
			strategy.getData().put("Legged time", Clock.getInstance().now());
			
		return legged;
	}
	
	protected PriceInstruction workLeggedPosition(MultiInstrumentStrategy strategy) {
		PriceInstruction pi = new PriceInstruction();
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			double position = instr.getPosition();
			double refQty = instr.get(double.class, "Ref qty");
			if(PriceUtils.Equal(Math.abs(position), refQty))
				continue;
			
			double qty = refQty - Math.abs(position);
			String symbol = instr.getSymbol();
			Quote quote = strategy.getQuote(symbol);
			OrderSide side = position>0?OrderSide.Buy:OrderSide.Sell;
			double price = QuoteUtil.getOppositePriceToQuantity(quote, qty, side);
			PriceAllocation pa = 
				new PriceAllocation(symbol, side, price, qty, ExchangeOrderType.LIMIT, instr.getId());
			pi.add(pa);
		}
		return pi;
	}
	
	protected PriceInstruction workZeroPosition(MultiInstrumentStrategy strategy, double short1Long2Ratio, double long1Short2Ratio) {
		if(!PriceUtils.isZero(short1Long2Ratio) && short1Long2Ratio > strategy.getData().get(double.class, "High take")) {
			strategy.logInfo("short1Long2Ratio: " + short1Long2Ratio);
			strategy.logInfo("long1Short2Ratio: " + long1Short2Ratio);
			strategy.logInfo(">>>>>>>> High Take Opportunity <<<<<<<<<<");
			return createShort1Long2(strategy);
		} else if (!PriceUtils.isZero(long1Short2Ratio) && long1Short2Ratio < strategy.getData().get(double.class, "Low take")) {
			strategy.logInfo("short1Long2Ratio: " + short1Long2Ratio);
			strategy.logInfo("long1Short2Ratio: " + long1Short2Ratio);
			strategy.logInfo(">>>>>>>> Low Take Opportunity <<<<<<<<<<");
			return createLong1Short2(strategy);
		}
		return null;
	}
	
	protected PriceInstruction createShort1Long2(MultiInstrumentStrategy strategy) {
		return createPriceInstruction(true, strategy);
	}
	protected PriceInstruction createLong1Short2(MultiInstrumentStrategy strategy) {
		return createPriceInstruction(false, strategy);
	}
	protected PriceInstruction createPriceInstruction(boolean short1Long2, MultiInstrumentStrategy strategy) {
		PriceInstruction pi = new PriceInstruction();
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			String symbol = instr.getSymbol();
			double qty = instr.get(double.class, "Ref qty");
			Quote quote = strategy.getQuote(symbol);
			OrderSide side;
			if(instr.get(int.class, "Leg") == 1) {
				side = short1Long2?OrderSide.Sell:OrderSide.Buy;
			} else {
				side = short1Long2?OrderSide.Buy:OrderSide.Sell;
			}
			double price = QuoteUtil.getOppositePriceToQuantity(quote, qty, side);
			PriceAllocation pa = 
				new PriceAllocation(symbol, side, price, qty, ExchangeOrderType.LIMIT, instr.getId());
			pi.add(pa);
		}
		return pi;
	}
	
	protected double calShort1Long2(MultiInstrumentStrategy strategy) {
		return calDelta(true, strategy);
	}
	
	protected double calLong1Short2(MultiInstrumentStrategy strategy) {
		return calDelta(false, strategy);
	}
	
	protected double calDelta(boolean short1Long2, MultiInstrumentStrategy strategy) {
		double leg1Value = 0;
		double leg2Value = 0;
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			String symbol = instr.getSymbol();
			Quote quote = strategy.getQuote(symbol);
			if(null == quote)
				return 0;
			if(instr.get(int.class, "Leg") == 1) {
				double value = QuoteUtil.getOppositeValueToQuantity(quote, instr.get(double.class, "Ref qty"), short1Long2 ? OrderSide.Sell : OrderSide.Buy);
				if(PriceUtils.isZero(value))
					return 0;
				leg1Value += value;
			} else {
				double value = QuoteUtil.getOppositeValueToQuantity(quote, instr.get(double.class, "Ref qty"), short1Long2 ? OrderSide.Buy : OrderSide.Sell);
				if(PriceUtils.isZero(value))
					return 0;
				leg2Value += value;
			}
		}
		double value = strategy.getData().get(double.class, "Value");
		return (leg1Value-value)/value - (leg2Value-value)/value;
	}

}
