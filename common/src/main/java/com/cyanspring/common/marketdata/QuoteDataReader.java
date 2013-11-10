package com.cyanspring.common.marketdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import com.cyanspring.common.type.QtyPrice;

public class QuoteDataReader implements ITickDataReader {
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final String fieldDelimiter = "=";
	private final String tokenDelimiter = ",";

	private class Pair {
		public Pair(String tag, String value) {
			super();
			this.tag = tag;
			this.value = value;
		}
		String tag, value;
	}
	
	@Override
	public Quote stringToQuote(String str) throws TickDataException {
		String[] tokens = str.split(tokenDelimiter);
		if(tokens.length < 1)
			throw new TickDataException("The tick data is less than one token");
		
		
		List<Pair> pairs = new LinkedList<Pair>();
		for(String token: tokens) {
			String[] tagValue = token.split(fieldDelimiter);
			if(tagValue.length != 2)
				throw new TickDataException("Tag Value malformatted: " + tagValue);
			
			pairs.add(new Pair(tagValue[0], tagValue[1]));
		}
		
		if(!pairs.get(0).tag.equals(TickField.SYMBOL.value()))
			throw new TickDataException("The first field must be symbol: " + str);
		
		String symbol = pairs.get(0).value;
		Quote quote = new Quote(symbol, new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		try {
			for(Pair pair: pairs) {
				if(pair.tag.equals(TickField.TIME.value())) {
					quote.setTimeStamp(timeFormat.parse(pair.value));
				} else if(pair.tag.equals(TickField.BID.value())) {
					quote.setBid(Double.parseDouble(pair.value));
				} else if(pair.tag.equals(TickField.BID_VOL.value())) {
					quote.setBidVol(Double.parseDouble(pair.value));
				}else if(pair.tag.equals(TickField.ASK.value())) {
					quote.setAsk(Double.parseDouble(pair.value));
				} else if(pair.tag.equals(TickField.ASK_VOL.value())) {
					quote.setAskVol(Double.parseDouble(pair.value));
				}else if(pair.tag.equals(TickField.LAST.value())) {
					quote.setLast(Double.parseDouble(pair.value));
				} else if(pair.tag.equals(TickField.LAST_VOL.value())) {
					quote.setLastVol(Double.parseDouble(pair.value));
				}else if(pair.tag.equals(TickField.HIGH.value())) {
					quote.setHigh(Double.parseDouble(pair.value));
				}else if(pair.tag.equals(TickField.LOW.value())) {
					quote.setLow(Double.parseDouble(pair.value));
				}else if(pair.tag.equals(TickField.OPEN.value())) {
					quote.setOpen(Double.parseDouble(pair.value));
				}else if(pair.tag.equals(TickField.CLOSE.value())) {
					quote.setClose(Double.parseDouble(pair.value));
				}else if(pair.tag.equals(TickField.TOTAL_VOL.value())) {
					quote.setTotalVolume(Double.parseDouble(pair.value));
				} else if(pair.tag.indexOf(TickField.BID.value()) == 0 &&
						pair.tag.indexOf(TickField.BID_VOL.value()) != 0) {
					String sub = pair.tag.substring(TickField.BID.value().length());
					int pos = Integer.parseInt(sub);
					if(pos != quote.getBids().size())
						throw new TickDataException("depth bid out of sequence: " + pos);
					quote.getBids().add(new QtyPrice(0, Double.parseDouble(pair.value)));
				} else if(pair.tag.indexOf(TickField.BID_VOL.value()) == 0) {
					String sub = pair.tag.substring(TickField.BID_VOL.value().length());
					int pos = Integer.parseInt(sub);
					if(pos != quote.getBids().size()-1)
						throw new TickDataException("depth bid vol out of sequence: " + pos);
					double price = quote.getBids().get(pos).getPrice();
					quote.getBids().set(pos, new QtyPrice(Double.parseDouble(pair.value), price));
				} else if(pair.tag.indexOf(TickField.ASK.value()) == 0 &&
						pair.tag.indexOf(TickField.ASK_VOL.value()) != 0) {
					String sub = pair.tag.substring(TickField.ASK.value().length());
					int pos = Integer.parseInt(sub);
					if(pos != quote.getAsks().size())
						throw new TickDataException("depth ask out of sequence: " + pos);
					quote.getAsks().add(new QtyPrice(0, Double.parseDouble(pair.value)));
				} else if(pair.tag.indexOf(TickField.ASK_VOL.value()) == 0) {
					String sub = pair.tag.substring(TickField.ASK_VOL.value().length());
					int pos = Integer.parseInt(sub);
					if(pos != quote.getAsks().size()-1)
						throw new TickDataException("depth ask vol out of sequence: " + pos);
					double price = quote.getAsks().get(pos).getPrice();
					quote.getAsks().set(pos, new QtyPrice(Double.parseDouble(pair.value), price));
				}
			}
		} catch (NumberFormatException e) {
			throw new TickDataException(e.getMessage());
		} catch (ParseException e) {
			throw new TickDataException(e.getMessage());
		}
		return quote;
	}

}
