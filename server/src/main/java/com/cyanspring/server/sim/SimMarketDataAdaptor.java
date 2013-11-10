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
package com.cyanspring.server.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;

import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.QtyPrice;

public class SimMarketDataAdaptor implements IMarketDataAdaptor {
	private Exchange exchange;
	Map<String, List<IMarketDataListener>> subs = 
		Collections.synchronizedMap(new HashMap<String, List<IMarketDataListener>>());

	Map<String, Quote> cache = Collections.synchronizedMap(new HashMap<String, Quote>());
	
	
	private Quote bookToQuote(OrderBook book) {
		List<QtyPrice> bids = new LinkedList<QtyPrice>();
		List<QtyPrice> asks = new LinkedList<QtyPrice>();
		for(Order order: book.getSumBidOrders()) {
			bids.add(new QtyPrice(order.getQuantity(), order.getPrice()));
		}
		for(Order order: book.getSumAskOrders()) {
			asks.add(new QtyPrice(order.getQuantity(), order.getPrice()));
		}
		Quote quote = new Quote(book.getCode(), bids, asks);
		quote.setBid(book.getBestBid());
		quote.setAsk(book.getBestAsk());
		quote.setBidVol(book.getBestBidVol());
		quote.setAskVol(book.getBestAskVol());
		quote.setHigh(book.getHigh());
		quote.setLast(book.getLast());
		quote.setLastVol(book.getLastVol());
		quote.setLow(book.getLow());
		quote.setOpen(book.getOpen());
		quote.setClose(book.getClose());
		quote.setTotalVolume(book.getTradedVolume());
		return quote;
	}
	ExchangeListener<OrderBook> orderBookListener = new ExchangeListener<OrderBook>() {

		@Override
		public void onChangeEvent(OrderBook book) {
			Quote quote = bookToQuote(book);
			
			List<IMarketDataListener> list = subs.get(book.getCode());
			if(null != list)
				for(IMarketDataListener listener: list)
					listener.onQuote((Quote)quote.clone());
			
			
		}
		
	};

	ExchangeListener<Trade> tradeListener = new ExchangeListener<Trade>() {

		@Override
		public void onChangeEvent(Trade t) {
			com.cyanspring.common.marketdata.Trade trade = new com.cyanspring.common.marketdata.Trade();
			trade.setSymbol(t.getAskOrder().getCode());
			trade.setPrice(t.getPrice());
			trade.setQuantity(t.getQuantity());
			trade.setId(new Long(t.getTradeID()).toString());
			List<IMarketDataListener> list = subs.get(t.getAskOrder().getCode());
			if(null != list)
				for(IMarketDataListener listener: list)
					listener.onTrade(trade.clone());
			
		}
		
	};

	public SimMarketDataAdaptor(Exchange exchange) {
		this.exchange = exchange;
		exchange.orderBookListenerKeeper.addExchangeListener(orderBookListener);
		exchange.tradeListenerKeeper.addExchangeListener(tradeListener);
	}
	
	@Override
	public void subscribeMarketData(String instrument,
			IMarketDataListener listener) {
		List<IMarketDataListener> list = subs.get(instrument);
		if (list == null) {
			list = Collections.synchronizedList(new ArrayList<IMarketDataListener>());
			subs.put(instrument, list);
		}

		if(!list.contains(listener))
			list.add(listener);
		
		listener.onState(true);
		OrderBook book = exchange.getBook(instrument);
		if(book != null) {
			Quote quote = bookToQuote(book);
			cache.put(instrument, quote);
			listener.onQuote((Quote)quote.clone());
		}
			
	}

	@Override
	public void unsubscribeMarketData(String instrument,
			IMarketDataListener listener) {
		List<IMarketDataListener> list = subs.get(instrument);
		if (list != null) {
			list.remove(listener);
		}
	}

	@Override
	public boolean getState() {
		// should depend on simulator state
		return true;
	}

	@Override
	public void init() {
	}

	@Override
	public void uninit() {
	}


}
