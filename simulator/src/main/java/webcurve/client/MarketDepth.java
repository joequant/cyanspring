package webcurve.client;

import java.io.Serializable;
import java.util.ArrayList;

public class MarketDepth implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5959935933714505587L;
	String symbol;
	
	public ArrayList<Double> getBids() {
		return bids;
	}
	public void setBids(ArrayList<Double> bids) {
		this.bids = bids;
	}
	public ArrayList<Double> getAsks() {
		return asks;
	}
	public void setAsks(ArrayList<Double> asks) {
		this.asks = asks;
	}
	public ArrayList<Long> getBidVols() {
		return bidVols;
	}
	public void setBidVols(ArrayList<Long> bidVols) {
		this.bidVols = bidVols;
	}
	public ArrayList<Long> getAskVols() {
		return askVols;
	}
	public void setAskVols(ArrayList<Long> askVols) {
		this.askVols = askVols;
	}	
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	private ArrayList<Double> bids = new ArrayList<Double>();
	private ArrayList<Double> asks = new ArrayList<Double>();
	private ArrayList<Long> bidVols = new ArrayList<Long>();
	private ArrayList<Long> askVols = new ArrayList<Long>();
	
	public double getBid()
	{
		if (bids.size() <= 0 )
			return 0.0;
		return bids.get(0);
	}
	
	public double getAsk()
	{
		if (asks.size() <= 0 )
			return 0.0;
		return asks.get(0);
	}
	
	public long getBidVol()
	{
		if (bidVols.size() <= 0 )
			return 0;
		return bidVols.get(0);
	}

	public long getAskVol()
	{
		if (askVols.size() <= 0 )
			return 0;
		return askVols.get(0);
	}
/*	
	public double getLastPrice() {
		return lastPrice;
	}
	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}
	public long getLastVolume() {
		return lastVolume;
	}
	public void setLastVolume(long lastVolume) {
		this.lastVolume = lastVolume;
	}

	private double lastPrice;
	private long lastVolume;
*/	
	public void print()
	{
		System.out.printf("%n                    %s                      %n", symbol);
		System.out.printf("--------------------------------------------%n");
		System.out.printf("         Buy                  Sell          %n");
		System.out.printf("--------------------------------------------%n");
		int i = 0;
		while(i< Math.max(bids.size(),asks.size()))
		{
			if (i < bids.size())
			{
				Double price = bids.get(i);
				Long qty = bidVols.get(i);
				System.out.printf("% 10d% 10.3f", qty, price);
			}
			else
				System.out.printf("                    ");
			System.out.printf(" | ");
			if (i < asks.size())
			{
				Double price = asks.get(i);
				Long qty = askVols.get(i);
				System.out.printf("%-10.3f% 10d", price, qty);
			}
			System.out.printf("%n");
			i++;
		}
		System.out.printf("--------------------------------------------%n%n");
	}
}
