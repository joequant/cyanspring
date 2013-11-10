package com.cyanspring.sample.custom;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;
import com.cyanspring.strategy.utils.QuoteUtil;

public class TrendingStrategy extends MultiInstrumentStrategy {
	protected Map<String, OCHL> currentOchls = new HashMap<String, OCHL>();
	protected Map<String, OCHL> prevOchls = new HashMap<String, OCHL>();
	protected Map<String, OCHL> highOchls = new HashMap<String, OCHL>();
	protected Date binTime;

	@Override
	public void init() throws StrategyException {
		super.init();
		
	}
	
	class OCHL {
		double open;
		double close;
		double high;
		double low;
		public OCHL(double open, double close, double high, double low) {
			super();
			this.open = open;
			this.close = close;
			this.high = high;
			this.low = low;
		}
		
		boolean isValid() {
			return  !PriceUtils.isZero(open) &&
					!PriceUtils.isZero(close) &&
					!PriceUtils.isZero(high) &&
					!PriceUtils.isZero(low);
		}
		
		double getAvg() {
			if(!isValid()) {
				logError("calling avg without a valid OCHL");
			}
			return (open + close + high + low)/4;
		}
		@Override
		public String toString() {
			return "" + open + "/" + close + "/" + high + "/" + low;
		}
	}
	
	private double getPrevDayValue(String symbol) {
		RefData refData = refDataManager.getRefData(symbol);
		Integer prevOpen = refData.get(Integer.class, RefDataField.OPEN.value());
		Integer prevClose = refData.get(Integer.class, RefDataField.CLOSE.value());
		Integer prevHigh = refData.get(Integer.class, RefDataField.HIGH.value());
		Integer prevLow = refData.get(Integer.class, RefDataField.LOW.value());
		if(null == prevOpen || null == prevClose || null == prevHigh || null == prevLow)
			return 0;
		if(PriceUtils.isZero(prevOpen) || 
		   PriceUtils.isZero(prevClose) || 	
		   PriceUtils.isZero(prevHigh) || 
		   PriceUtils.isZero(prevLow)
		) {
			return 0;
		}
		return(prevOpen + prevClose + prevHigh + prevLow)/4;
	}
	
	private double getTodayValue(Quote quote) {
		if(PriceUtils.isZero(quote.getOpen()) || 
				   PriceUtils.isZero(quote.getHigh()) || 
				   PriceUtils.isZero(quote.getLow())
				) {
				return 0;
			}
		return (quote.getOpen() + quote.getLast() + quote.getHigh() + quote.getLow())/4;
	}
	@Override
	protected PriceInstruction analyze() {
		PriceInstruction pi = new PriceInstruction();
		for(Instrument instr: data.getInstrumentData().values()) {
			double prevDayValue = getPrevDayValue(instr.getSymbol());
			if(PriceUtils.isZero(prevDayValue)) {
				logWarn("Yesterday's OPEN/CLOSE/HIGH/LOW are not set, strategy will not run properly");
				continue;
			}
			
			Quote quote = quotes.get(instr.getSymbol());
			double currentDayValue = getTodayValue(quote);
			if(PriceUtils.isZero(currentDayValue)) {
				logWarn("Today's OPEN/HIGH/LOW are not yet set, strategy skips");
				continue;
			}
			
			OCHL prevOchl = prevOchls.get(instr.getSymbol());
			if(null == prevOchl || !prevOchl.isValid()) {
				logDebug("Previous OCHL invalid " + prevOchl);
				continue;
			}
			OCHL currentOchl = currentOchls.get(instr.getSymbol());
			if(null == currentOchl || !currentOchl.isValid()) {
				logDebug("Current OCHL invalid " + currentOchl);
				continue;
			}
			
			double position = instr.getPosition();
			double qty = instr.get(double.class, "Qty");
			if(PriceUtils.GreaterThan(qty, position)) { // opportunity to entry
				logDebug("Checking entry...");
				if(PriceUtils.GreaterThan(currentOchl.getAvg(), prevOchl.getAvg()) && 
						PriceUtils.GreaterThan(currentDayValue, prevDayValue)) {
					logInfo(">>>>>>>>> Entry <<<<<<<<<");
					double orderQty = qty - position;
					double price = QuoteUtil.getOppositePriceToQuantity(quote, orderQty, OrderSide.Buy);
					if (price != 0.0) {
						PriceAllocation pa = new PriceAllocation(instr.getSymbol(), OrderSide.Buy, price, 
								orderQty, ExchangeOrderType.LIMIT, instr.getId());
						pi.add(pa);
					} else {
						logWarn("Market dpeth can't fill quantity: " + orderQty);
					}
				}
			}
			
			if(!PriceUtils.isZero(position)) { // opportunity to exit
				logDebug("Checking exit...");
				OCHL highOchl = highOchls.get(instr.getSymbol());
				double exitFactor = instr.get(double.class, "Exit delta");
				if(null == highOchl)
					continue;
				double exitingPrice = highOchl.getAvg() * (1-exitFactor);
				if(null != highOchl) {
					if( PriceUtils.EqualGreaterThan(exitingPrice, currentOchl.getAvg())) {
						logInfo(">>>>>>>>> Exiting <<<<<<<<<");
						PriceAllocation pa = new PriceAllocation(instr.getSymbol(), OrderSide.Sell, quote.getBid(), 
								position, ExchangeOrderType.LIMIT, instr.getId());
						pi.add(pa);
					}
				}
			}
			
		}
		return pi;
	}
	
	@Override
	protected void processAsyncTimerEvent(AsyncTimerEvent event) {
		super.processAsyncTimerEvent(event);
		
		int interval = data.get(int.class, "Bin Size");
		if(null == binTime || TimeUtil.getTimePass(binTime) > interval * 1000) {
			binTime = Clock.getInstance().now();
			for(Instrument instr: data.getInstrumentData().values()) {
				logInfo("Moving to next time bin");
				Quote quote = quotes.get(instr.getSymbol());
				if(!validQuote(quote)) {
					logDebug("Quote not yet valid: " + quote);
					return;
				}
				
				OCHL prevOchl = prevOchls.get(instr.getSymbol());
				logInfo(instr.getSymbol() + " previous OCHL " + prevOchl + ", avg: " + (prevOchl==null?0:prevOchl.getAvg()));

				OCHL ochl = currentOchls.get(instr.getSymbol());
				if(null != ochl) { 
					// setting close of current time bin
					ochl.close = quote.getLast();
				} 
				logInfo(instr.getSymbol() + " current OCHL " + ochl + ", avg: " + (ochl==null?0:ochl.getAvg()));
				logInfo(instr.getSymbol() + " previous day value: " + getPrevDayValue(instr.getSymbol()));
				logInfo(instr.getSymbol() + " today day value: " + getTodayValue(quote));
				OCHL highOchl = highOchls.get(instr.getSymbol());
				logInfo(instr.getSymbol() + " high OCHL " + highOchl + ", avg: " + (highOchl==null?0:highOchl.getAvg()));
				if(null != highOchl) {
					double exitFactor = instr.get(double.class, "Exit delta");
					double exitingPrice = highOchl.getAvg() * (1-exitFactor);
					logInfo(instr.getSymbol() + " exit price: " + exitingPrice);
				}

			}
			execute(ExecuteTiming.NOW);
			
			// after execution we move to next time bin
			for(Instrument instr: data.getInstrumentData().values()) {
				Quote quote = quotes.get(instr.getSymbol());
				if(!validQuote(quote)) {
					logDebug("Quote not yet valid: " + quote);
					return;
				}
				OCHL ochl = currentOchls.get(instr.getSymbol());
				if(null != ochl) { // moving to next time bin
					ochl.close = quote.getLast();
					prevOchls.put(instr.getSymbol(), ochl);
					currentOchls.put(instr.getSymbol(), new OCHL(quote.getLast(), 0, quote.getLast(), quote.getLast()));
					
					double position = instr.getPosition();
					if(PriceUtils.GreaterThan(position, 0)){
						OCHL highOchl = highOchls.get(instr.getSymbol());
						if(null == highOchl || PriceUtils.GreaterThan(ochl.getAvg(), highOchl.getAvg())) {
							highOchls.put(instr.getSymbol(), ochl);
						}
					} else {
						highOchls.remove(instr.getSymbol());
					}
				} 
				
				ochl = new OCHL(quote.getLast(), 0, quote.getLast(), quote.getLast());
				currentOchls.put(instr.getSymbol(), ochl);
			}
		}
	}
	
	private boolean validQuote(Quote quote) {
		return quote != null && !PriceUtils.isZero(quote.getOpen()) &&
				!PriceUtils.isZero(quote.getHigh()) &&
				!PriceUtils.isZero(quote.getLow()) &&
				!PriceUtils.isZero(quote.getLast());
	}
	
	@Override
	protected void processQuote(Quote quote) {
		super.processQuote(quote);

		OCHL ochl = currentOchls.get(quote.getSymbol());
		if(null != ochl) {
			if(PriceUtils.GreaterThan(quote.getLast(), ochl.high))
				ochl.high = quote.getLast();
			
			if(PriceUtils.LessThan(quote.getLast(), ochl.low))
				ochl.low = quote.getLast();
			
			ochl.close = quote.getLast();
		}
	}
}
