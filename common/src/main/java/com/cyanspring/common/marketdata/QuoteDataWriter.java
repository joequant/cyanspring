package com.cyanspring.common.marketdata;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import com.cyanspring.common.util.PriceUtils;

public class QuoteDataWriter implements ITickDataWriter {
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final static DecimalFormat decimalFormat = new DecimalFormat("#.########");
	private final String fieldDelimiter = "=";
	private final String tokenDelimiter = ",";
	
	private boolean valid(double value) {
		return !PriceUtils.isZero(value);
	}
	
	private boolean valid(Object obj) {
		return null != obj;
	}

	private void appendValue(TickField field, double value, StringBuilder sb) {
		if(valid(value)) {
			sb.append(field.value());
			sb.append(fieldDelimiter);
			sb.append(decimalFormat.format(value));
			sb.append(tokenDelimiter);
		} 
	}
	
	@Override
	public String quoteToString(Quote quote) {
		StringBuilder sb = new StringBuilder();
		sb.append(TickField.SYMBOL.value());
		sb.append(fieldDelimiter);
		sb.append(quote.getSymbol());
		sb.append(tokenDelimiter);
		
		
		if(valid(quote.getTimeStamp())) {
			sb.append(TickField.TIME.value());
			sb.append(fieldDelimiter);
			sb.append(timeFormat.format(quote.getTimeStamp()));
			sb.append(tokenDelimiter);
		} 
		
		double value;
		value = quote.getBid();
		appendValue(TickField.BID, value, sb);
		
		value = quote.getBidVol();
		appendValue(TickField.BID_VOL, value, sb);
		
		value = quote.getAsk();
		appendValue(TickField.ASK, value, sb);
		
		value = quote.getAskVol();
		appendValue(TickField.ASK_VOL, value, sb);
		
		value = quote.getLast();
		appendValue(TickField.LAST, value, sb);
		
		value = quote.getLastVol();
		appendValue(TickField.LAST_VOL, value, sb);
		
		value = quote.getHigh();
		appendValue(TickField.HIGH, value, sb);
		
		value = quote.getLow();
		appendValue(TickField.LOW, value, sb);
		
		value = quote.getOpen();
		appendValue(TickField.OPEN, value, sb);
		
		value = quote.getClose();
		appendValue(TickField.CLOSE, value, sb);
		
		value = quote.getTotalVolume();
		appendValue(TickField.TOTAL_VOL, value, sb);
		
		for(int i=0; i<quote.getBids().size(); i++) {
			sb.append(TickField.BID.value() + i);
			sb.append(fieldDelimiter);
			sb.append(quote.getBids().get(i).price);
			sb.append(tokenDelimiter);
			
			sb.append(TickField.BID_VOL.value() + i);
			sb.append(fieldDelimiter);
			sb.append(quote.getBids().get(i).quantity);
			sb.append(tokenDelimiter);
		}
		
		for(int i=0; i<quote.getAsks().size(); i++) {
			sb.append(TickField.ASK.value() + i);
			sb.append(fieldDelimiter);
			sb.append(quote.getAsks().get(i).price);
			sb.append(tokenDelimiter);
			
			sb.append(TickField.ASK_VOL.value() + i);
			sb.append(fieldDelimiter);
			sb.append(quote.getAsks().get(i).quantity);
			sb.append(tokenDelimiter);
		}
		return sb.toString();
	}

}
