package webcurve.marketdata;

import java.util.ArrayList;

import webcurve.common.QuantityPrice;

public class Quote implements Cloneable{

	String Symbol;
	double bid;
	double ask;
	long bidVol;
	long askVol;
	double last;
	long lastVol;
	double high;
	double low;
	double open;
	double close;
	
	public String getSymbol() {
		return Symbol;
	}
	public void setSymbol(String symbol) {
		Symbol = symbol;
	}
	public double getBid() {
		return bid;
	}
	public void setBid(double bid) {
		this.bid = bid;
	}
	public double getAsk() {
		return ask;
	}
	public void setAsk(double ask) {
		this.ask = ask;
	}
	public long getBidVol() {
		return bidVol;
	}
	public void setBidVol(long bidVol) {
		this.bidVol = bidVol;
	}
	public long getAskVol() {
		return askVol;
	}
	public void setAskVol(long askVol) {
		this.askVol = askVol;
	}
	public double getLast() {
		return last;
	}
	public void setLast(double last) {
		this.last = last;
	}
	public long getLastVol() {
		return lastVol;
	}
	public void setLastVol(long lastVol) {
		this.lastVol = lastVol;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}

	ArrayList<QuantityPrice> bids = new ArrayList<QuantityPrice>();
	ArrayList<QuantityPrice> asks = new ArrayList<QuantityPrice>();
	
	public ArrayList<QuantityPrice> getBids() {
		return bids;
	}
	public void setBids(ArrayList<QuantityPrice> bids) {
		this.bids = bids;
	}
	public ArrayList<QuantityPrice> getAsks() {
		return asks;
	}
	public void setAsks(ArrayList<QuantityPrice> asks) {
		this.asks = asks;
	}
	
	// need deep copy here~!
	public Quote clone()
	{
		try {
			Quote quote = (Quote)super.clone();
			ArrayList<QuantityPrice> newBids = new ArrayList<QuantityPrice>();
			for (QuantityPrice qp: bids)
				newBids.add(qp.clone());
			ArrayList<QuantityPrice> newAsks = new ArrayList<QuantityPrice>();
			for (QuantityPrice qp: asks)
				newAsks.add(qp.clone());
			quote.setBids(newBids);
			quote.setAsks(newAsks);
			return quote;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void print()
	{
		System.out.printf("%n                    %s                      %n", getSymbol());
		System.out.printf("--------------------------------------------%n");
		System.out.printf("         Buy                  Sell          %n");
		System.out.printf("--------------------------------------------%n");
		System.out.printf("[%d, %f], [%d, %f], [%d, %f]%n", bidVol, bid, askVol, ask, lastVol, last);
		System.out.printf("--------------------------------------------%n");
		
		for(int i=0; i<Math.max(bids.size(), asks.size()); i++)
		{
			if (i < bids.size())
			{
				QuantityPrice qp = bids.get(i);
				System.out.printf("% 10d% 10.3f", qp.getQuantity(), qp.getPrice());
			}
			else
				System.out.printf("                         ");
			System.out.printf(" | ");
			if (i < asks.size())
			{
				QuantityPrice qp = asks.get(i);
				System.out.printf("% 10d% 10.3f", qp.getQuantity(), qp.getPrice());
			}

			System.out.printf("%n");
		}
		System.out.printf("--------------------------------------------%n%n");		
	}
}
