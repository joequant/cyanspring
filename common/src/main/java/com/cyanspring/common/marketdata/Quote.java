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
package com.cyanspring.common.marketdata;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.IdGenerator;

public class Quote implements Cloneable{
	private static final Logger log = LoggerFactory
			.getLogger(Quote.class);
	String id = IdGenerator.getInstance().getNextID();
	
	String symbol;
	double bid;
	double ask;
	double bidVol;
	double askVol;
	double last;
	double lastVol;
	double high;
	double low;
	double open;
	double close;
	double totalVolume;
	Date timeStamp;
	Date timeSent;
	
	public String getSymbol() {
		return symbol;
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
	public double getBidVol() {
		return bidVol;
	}
	public void setBidVol(double bidVol) {
		this.bidVol = bidVol;
	}
	public double getAskVol() {
		return askVol;
	}
	public void setAskVol(double askVol) {
		this.askVol = askVol;
	}
	public double getLast() {
		return last;
	}
	public void setLast(double last) {
		this.last = last;
	}
	public double getLastVol() {
		return lastVol;
	}
	public void setLastVol(double lastVol) {
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

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	protected List<QtyPrice> bids;
	protected List<QtyPrice> asks;
	
	
	public Quote(String symbol, List<QtyPrice> bids, List<QtyPrice> asks) {
		this.symbol = symbol;
		this.bids = bids;
		this.asks = asks;
		this.timeStamp = new Date();
		this.timeSent = this.timeStamp;
	}
	
	public List<QtyPrice> getBids() {
		return bids;
	}
	public List<QtyPrice> getAsks() {
		return asks;
	}
	
	public Date getTimeSent() {
		return timeSent;
	}
	public void setTimeSent(Date timeSent) {
		this.timeSent = timeSent;
	}
	
	public double getTotalVolume() {
		return totalVolume;
	}
	public void setTotalVolume(double totalVolume) {
		this.totalVolume = totalVolume;
	}

	public String toString()
	{
		return id + " - " + symbol + " : [" + bidVol + "@" + bid + "," + askVol + "@" + ask + "]"
			+ bids + asks;
	}
	
	public void print()
	{
		System.out.printf("%n                 %s                      %n", getSymbol());
		System.out.printf("--------------------------------------------%n");
		System.out.printf("         Buy                  Sell          %n");
		System.out.printf("--------------------------------------------%n");
		System.out.printf("[%f, %f], [%f, %f], [%f, %f]%n", bidVol, bid, askVol, ask, lastVol, last);
		System.out.printf("--------------------------------------------%n");
		
		for(int i=0; i<Math.max(bids.size(), asks.size()); i++)
		{
			if (i < bids.size())
			{
				QtyPrice qp = bids.get(i);
				System.out.printf("% 10.0f% 10.3f", qp.getQuantity(), qp.getPrice());
			}
			else
				System.out.printf("                    ");
			System.out.printf(" | ");
			if (i < asks.size())
			{
				QtyPrice qp = asks.get(i);
				System.out.printf("% 10.0f% 10.3f", qp.getQuantity(), qp.getPrice());
			}

			System.out.printf("%n");
		}
		System.out.printf("--------------------------------------------%n%n");		
	}

	@Override
	public Object clone() { //deep copy
		Quote result;
		try {
			result = (Quote)super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
		result.bids = new LinkedList<QtyPrice>();
		for(QtyPrice qp: bids) {
			result.bids.add(new QtyPrice(qp.quantity, qp.price));
		}
		result.asks = new LinkedList<QtyPrice>();
		for(QtyPrice qp: asks) {
			result.asks.add(new QtyPrice(qp.quantity, qp.price));
		}
		return result;
	}
}
