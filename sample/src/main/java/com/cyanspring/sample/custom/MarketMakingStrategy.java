package com.cyanspring.sample.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;

public class MarketMakingStrategy extends MultiInstrumentStrategy {
	private static final Logger log = LoggerFactory
			.getLogger(MarketMakingStrategy.class);
	
	private Instrument getDerivative(int leg) {
		for(Instrument instr: data.getInstrumentData().values()) {
			if(instr.get(int.class, "Leg") == leg && instr.get(boolean.class, "Derivative"))
				return instr;
		}
		return null;
	}
	
	private double calDerivativePrice(double price) {
		return price / 10;
	}
	
	@Override
	public Logger getLog() {
		return log;
	}
	
	@Override
	protected PriceInstruction analyze() {
		PriceInstruction pi = new PriceInstruction();
		for(Instrument instr: data.getInstrumentData().values()) {
			if(!instr.get(boolean.class, "Derivative")) {
				String symbol = instr.getSymbol();
				int leg = instr.get(int.class, "Leg");
				Instrument dv = getDerivative(leg);
				double qty = dv.get(double.class, "Qty");
				
				Quote quote = getQuotes().get(symbol);
				double lastPrice = quote.getLast();
				if(PriceUtils.isZero(lastPrice)) {
					logInfo("Last price of underlying stock " + symbol + " isn't available yet...");
					continue;
				}
				double dvPrice = calDerivativePrice(lastPrice);			
				ITickTable tickTable = tickTableManager.getTickTable(symbol);
				
				// working out bids
				PriceAllocation pa;
				double bidPrice = tickTable.tickDown(dvPrice, 2, false);
				pa = new PriceAllocation(dv.getSymbol(), OrderSide.Buy, bidPrice, qty, ExchangeOrderType.LIMIT, dv.getId());
				pi.add(pa);
				bidPrice = tickTable.tickDown(bidPrice, false);
				pa = new PriceAllocation(dv.getSymbol(), OrderSide.Buy, bidPrice, qty, ExchangeOrderType.LIMIT, dv.getId());
				pi.add(pa);
				bidPrice = tickTable.tickDown(bidPrice, false);
				pa = new PriceAllocation(dv.getSymbol(), OrderSide.Buy, bidPrice, qty, ExchangeOrderType.LIMIT, dv.getId());
				pi.add(pa);
				
				//working out asks
				double askPrice = tickTable.tickUp(dvPrice, 2, true);
				pa = new PriceAllocation(dv.getSymbol(), OrderSide.Sell, askPrice, qty, ExchangeOrderType.LIMIT, dv.getId());
				pi.add(pa);
				askPrice = tickTable.tickUp(askPrice, true);
				pa = new PriceAllocation(dv.getSymbol(), OrderSide.Sell, askPrice, qty, ExchangeOrderType.LIMIT, dv.getId());
				pi.add(pa);
				askPrice = tickTable.tickUp(askPrice, true);
				pa = new PriceAllocation(dv.getSymbol(), OrderSide.Sell, askPrice, qty, ExchangeOrderType.LIMIT, dv.getId());
				pi.add(pa);
			}
		}
		
		return pi;
	}
}
