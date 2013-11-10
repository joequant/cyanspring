package com.cyanspring.sample.singleinstrument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleinstrument.SingleInstrumentStrategy;

public class StopWinLossStrategy extends SingleInstrumentStrategy {
	private static final Logger log = LoggerFactory
			.getLogger(StopWinLossStrategy.class);
	// fields for this strategy
	public static final String FIELD_HIGH_FALL = "High fall";
	public static final String FIELD_HIGH_FALL_PERCENT = "High fall%";
	public static final String FIELD_MIN_WIN = "Min win";
	public static final String FIELD_MIN_WIN_PERCENT = "Min win%";
	public static final String FIELD_LOW_FALL = "Low fall";
	public static final String FIELD_LOW_FALL_PERCENT = "Low fall%";

	protected void validate() throws StrategyException {
		super.validate();
		
		Double avgPrice = instrument.getPosAvgPx();
		if(null == avgPrice || PriceUtils.isZero(avgPrice)) {
			throw new StrategyException("Avg postion price is 0");
		}

		Double mw = instrument.get(Double.class, FIELD_MIN_WIN);
		Double mwp = instrument.get(Double.class, FIELD_MIN_WIN_PERCENT);
		if(null == mw && null == mwp)
			throw new StrategyException("Min win and Min win% can't be both null");

		Double lf = instrument.get(Double.class, FIELD_LOW_FALL);
		Double lfp = instrument.get(Double.class, FIELD_LOW_FALL_PERCENT);
		if(null == lf && null == lfp)
			throw new StrategyException("Low fall and Low fall% can't be both null");

		// pre-calculation
		if(null == mw) {
			mw = avgPrice * mwp / 100.0;
			instrument.put(FIELD_MIN_WIN, mw);
		}
		
		Double hf = instrument.get(Double.class, FIELD_HIGH_FALL);
		Double hfp = instrument.get(Double.class, FIELD_HIGH_FALL_PERCENT);
		if(null == hf ) {
			if(null != hfp){
				hf = avgPrice * hfp / 100.0;
				instrument.put(FIELD_HIGH_FALL, hf);
			}
		}
		
		if(null == hf) {
			instrument.put(FIELD_HIGH_FALL, mw/4.0);
		}

		if(null == lf) {
			lf = avgPrice * lfp / 100.0;
			instrument.put(FIELD_LOW_FALL, lf);
		}
			
	}
	
	private void updateAllTimeHighLow(Quote quote) {
		if(PriceUtils.isZero(quote.getLast()))
			return;
		if(instrument.getSymbol().equals(quote.getSymbol())) {
			double ah = instrument.get(0.0, double.class, OrderField.AHIGH.value());
			double al = instrument.get(0.0, double.class, OrderField.ALOW.value());
			double last = quote.getLast();
//				double high = quote.getHigh();
//				double low = quote.getLow();
			if(PriceUtils.GreaterThan(last, ah)) {
				instrument.put(OrderField.AHIGH.value(), last);
				this.sendStrategyUpdate();
			}
//				if(high > ah)
//					instr.put(OrderField.AHIGH.value(), high);

			if(PriceUtils.isZero(al) || PriceUtils.LessThan(last, al)) {
				instrument.put(OrderField.ALOW.value(), last);
				this.sendStrategyUpdate();
			}
//				if(PriceUtils.isZero(al) || low < al)
//					instr.put(OrderField.ALOW.value(), low);

		}
	}
	
	@Override
	protected void processQuoteEvent(QuoteEvent event) {
		updateAllTimeHighLow(event.getQuote());
		super.processQuoteEvent(event);
	}	
	
	@Override
	protected PriceInstruction analyze() {
		PriceInstruction pi = new PriceInstruction();
		
		double ah = instrument.get(0.0, double.class, OrderField.AHIGH.value());
		double al = instrument.get(0.0, double.class, OrderField.ALOW.value());
		if(PriceUtils.isZero(ah) || PriceUtils.isZero(al))
			return null;

		double last = quote.getLast();
		if(PriceUtils.isZero(last))
			return null;
		
		double position = instrument.getPosition();
		if(PriceUtils.GreaterThan(position, 0)) {
			Double avgPrice = instrument.getPosAvgPx();

			// check high fall
			Double mw = instrument.get(Double.class, FIELD_MIN_WIN);
			Double hf = instrument.get(Double.class, FIELD_HIGH_FALL);
			
			boolean hfcase =
				PriceUtils.EqualGreaterThan(ah, avgPrice + mw) &&
				PriceUtils.EqualGreaterThan(ah - last, hf.doubleValue());
			
			if(hfcase)
				this.logInfo("High fall case detected");
			
			
			// check low fall
			Double lf = instrument.get(Double.class, FIELD_LOW_FALL);
			Double lfp = instrument.get(Double.class, FIELD_LOW_FALL_PERCENT);
			
			if(null == lf)
				lf = lfp * avgPrice / 100;
			
			boolean lfcase = PriceUtils.EqualGreaterThan(avgPrice - last, lf.doubleValue());
			if(lfcase)
				this.logInfo("Low fall case detected");
			
			if(hfcase || lfcase) {
				double price = quote.getBid();
				if(PriceUtils.isZero(price))
					price = quote.getLast();
				else if(PriceUtils.GreaterThan(position, quote.getBidVol()))
					price = tickTable.tickDown(price, false);
				
				PriceAllocation pa = instrument.createPriceAllocation(OrderSide.Sell, price, position, ExchangeOrderType.LIMIT);
				pi.add(pa);
			}
		}
		return pi;
	}
	
}
