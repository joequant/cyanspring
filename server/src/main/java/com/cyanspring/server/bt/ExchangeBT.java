package com.cyanspring.server.bt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.marketdata.IMarketDataAdaptor;
import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.ITickDataReader;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteDataReader;
import com.cyanspring.common.marketdata.TickDataException;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.stream.IStreamAdaptor;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.OrderUtils;
import com.cyanspring.strategy.utils.QuoteUtil;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webcurve.util.PriceUtils;

public class ExchangeBT implements IMarketDataAdaptor, IStreamAdaptor<IDownStreamConnection>{
	private static final Logger log = LoggerFactory
			.getLogger(ExchangeBT.class);
	private ITickDataReader tickDataReader = new QuoteDataReader();
	private PriorityQueue<Quote> quotes;
	private Map<String, BufferedReader> readers = new HashMap<String, BufferedReader>();
	protected Map<String, Quote> currentQuotes = new HashMap<String, Quote>();
	protected IMarketDataListener mdListener; // supports only one listener
	private IDownStreamListener dsListener;
	private Map<String, DualSet> exchangeOrders = new HashMap<String, DualSet>();
	private List<OrderAck> orderAcks = new LinkedList<OrderAck>();
	
	
	class DualSet {
		Set<ChildOrder> bids = new TreeSet<ChildOrder>(OrderUtils.childOrderComparator);
		Set<ChildOrder> asks = new TreeSet<ChildOrder>(OrderUtils.childOrderComparator);
		public void add(ChildOrder order) {
			if(order.getSide().isBuy()) {
				bids.add(order);
			} else {
				asks.add(order);
			}
		}
	}
	
	class OrderAck {
		ExecType execType;
		ChildOrder order; 
		Execution execution; 
		String message;
		public OrderAck(ExecType execType, ChildOrder order,
				Execution execution, String message) {
			super();
			this.execType = execType;
			this.order = order;
			this.execution = execution;
			this.message = message;
		}
		
	}
	
	public void loadTickDataFiles(String[] files) throws TickDataException, IOException {
		for(String fileName: files) {
			File file = new File(fileName);
			if(!file.exists()) {
				throw new TickDataException("Tick data file doesn't exist: " + fileName);
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			Quote quote = tickDataReader.stringToQuote(line);
			if(readers.containsKey(quote.getSymbol())) {
				throw new TickDataException("Duplicated tick data file: " + fileName);
			}
			readers.put(quote.getSymbol(), reader);
			exchangeOrders.put(quote.getSymbol(), new DualSet());
			quotes.add(quote);
			log.info("Tick file loaded: " + fileName);
		}
	}
	
	public void replay() {
		Quote quote;
		while((quote = quotes.poll())!= null) {
			processQuote(quote);
			
			BufferedReader reader = readers.get(quote.getSymbol());
			Quote nextQuote;
			try {
				String line = reader.readLine();
				if(null == line) {
					readers.remove(quote.getSymbol());
					reader.close();
					continue;
				}
				nextQuote = tickDataReader.stringToQuote(line);
			} catch (TickDataException e) {
				log.warn(e.getMessage());
				readers.remove(quote.getSymbol());
				continue;
			} catch (IOException e) {
				log.warn(e.getMessage());
				readers.remove(quote.getSymbol());
				continue;
			} 
			quotes.add(nextQuote);
		}
		log.info("Finished replay of tick files");
	}
	
	public void setQuote(Quote quote) {
		processQuote(quote);
	}
	
	public void processQuote(Quote quote) {
		if(Clock.getInstance().isManual())
			Clock.getInstance().setManualClock(quote.getTimeStamp());
		
		Quote prevQuote = currentQuotes.put(quote.getSymbol(), quote);

		// try to match existing orders in market
		DualSet dualSet = exchangeOrders.get(quote.getSymbol());
		if(null != dualSet) {
			List<ChildOrder> toBeRemoved = new LinkedList<ChildOrder>();
			for(ChildOrder order: dualSet.bids) {
				fillLimitOrder(order, quote, true);
				if(PriceUtils.isZero(order.getRemainingQty()))
					toBeRemoved.add(order);
			}
			
			for(ChildOrder order: toBeRemoved) {
				dualSet.bids.remove(order);
			}
	
			toBeRemoved.clear();
			for(ChildOrder order: dualSet.asks) {
				fillLimitOrder(order, quote, true);
				if(PriceUtils.isZero(order.getRemainingQty()))
					toBeRemoved.add(order);
			}
			
			for(ChildOrder order: toBeRemoved) {
				dualSet.asks.remove(order);
			}
	
			sendOrderAcks();
		}
		
		// call back listener
		if(null != mdListener) {
			mdListener.onQuote(quote);
			//work out trade
			if(null != prevQuote && quote.getTotalVolume() > prevQuote.getTotalVolume()) {
				Trade trade = new Trade();
				trade.setSymbol(quote.getSymbol());
				trade.setPrice(quote.getLast());
				trade.setQuantity(quote.getLastVol());
				trade.setId(IdGenerator.getInstance().getNextID() + "T");
				mdListener.onTrade(trade);
			}
		} 
		
	}
	
	public void reset() {
		quotes.clear();
		readers.clear();
		currentQuotes.clear();
		mdListener = null;
		dsListener = null;
		exchangeOrders.clear();
	}
	
	private void ackOrder(ExecType execType, ChildOrder order, Execution execution, String message) {
		if(null != dsListener)
			orderAcks.add(new OrderAck(execType, order.clone(), execution, message));
	}
	
	private void sendOrderAcks() {
			while(orderAcks.size()>0) {
				OrderAck ack = orderAcks.remove(0);
				if(null != dsListener)
					dsListener.onOrder(ack.execType, ack.order, ack.execution, ack.message);
			}
	}

	private void fillOrder(ChildOrder order, double price, double qty) {
		Execution execution = new Execution(order.getSymbol(), order.getSide(), qty,
				price, order.getId(), order.getParentOrderId(), order.getStrategyId(),
				IdGenerator.getInstance().getNextID() + "E");
		
		double remainingQty = order.getRemainingQty();
		double cumQty = order.getCumQty();
		double avgPx = order.getAvgPx();
		avgPx = (avgPx * cumQty + execution.getPrice() * execution.getQuantity()) / (cumQty + execution.getQuantity());
		cumQty += execution.getQuantity();
		order.setCumQty(cumQty);
		order.setAvgPx(avgPx);
		log.debug("cumtQty, avgPx: " + cumQty + ", " + avgPx);
		order.touch();

		if(PriceUtils.Equal(qty, remainingQty)) {
			order.setOrdStatus(OrdStatus.FILLED);
			ackOrder(ExecType.FILLED, order, execution, "");
		} else {
			order.setOrdStatus(OrdStatus.PARTIALLY_FILLED);
			ackOrder(ExecType.PARTIALLY_FILLED, order, execution, "");
		}
	}
	
	private void fillOrderWithLevelOneLiquidity(ChildOrder order, double price, double qty, Quote quote) {
		if(order.getSide().isBuy()) {
			quote.setAskVol(quote.getAskVol()-qty);
			if(PriceUtils.EqualLessThan(quote.getAskVol(), 0)) {
				quote.setAsk(0);
				quote.setAskVol(0);
			}
		} else {
			quote.setBidVol(quote.getBidVol()-qty);
			if(PriceUtils.EqualLessThan(quote.getBidVol(), 0)) {
				quote.setBid(0);
				quote.setBidVol(0);
			}
		}
		fillOrder(order, price, qty);
	}
	
	private void deductQuote(Quote quote) {
		List<QtyPrice> list = quote.getBids();
		while(list.size()>0) {
			QtyPrice qp = list.get(0);
			if(PriceUtils.isZero(qp.getQuantity()))
				list.remove(0);
			else
				break;
		}
		
		if(list.size()>0) {
			quote.setBid(list.get(0).price);
			quote.setBidVol(list.get(0).quantity);
		} else {
			quote.setBid(0);
			quote.setBidVol(0);
		}
		
		list = quote.getAsks();
		while(list.size()>0) {
			QtyPrice qp = list.get(0);
			if(PriceUtils.isZero(qp.getQuantity()))
				list.remove(0);
			else
				break;
		}
		
		if(list.size()>0) {
			quote.setAsk(list.get(0).price);
			quote.setAskVol(list.get(0).quantity);
		} else {
			quote.setAsk(0);
			quote.setAskVol(0);
		}
	}
	
	private void fillMarketOrder(ChildOrder order, Quote quote) {
		OrderSide side = order.getSide();
		List<QtyPrice> list = side.isBuy()?quote.getAsks():quote.getBids();
		double fillQty = 0;
		order.setOrdStatus(OrdStatus.NEW);
		ackOrder(ExecType.NEW, order, null, "");
		//if no level 2 data
		if(null == list || list.size() == 0 || PriceUtils.isZero(list.get(0).getQuantity())) {
			double fillPrice = side.isBuy()?quote.getAsk():quote.getBid();
			double liquidity = side.isBuy()?quote.getAskVol():quote.getBidVol();
			if(PriceUtils.isZero(liquidity)) {
				// do nothing
			} else if(PriceUtils.EqualGreaterThan(liquidity, order.getRemainingQty())) {
				fillQty = order.getRemainingQty();
				fillOrderWithLevelOneLiquidity(order, fillPrice, fillQty, quote);
			} else {
				fillQty = liquidity;
				fillOrderWithLevelOneLiquidity(order, fillPrice, fillQty, quote);
			} 
			
		} else { // work with level 2 data
			for(QtyPrice qp: list) {
				double remainingQty = order.getRemainingQty();
				if(PriceUtils.EqualLessThan(remainingQty, qp.getQuantity())) {
					fillOrder(order, qp.getPrice(), remainingQty);
					qp.setQuantity(qp.getQuantity() - remainingQty);
					break;
				}
					
				fillOrder(order, qp.getPrice(), qp.getQuantity());
				qp.setQuantity(0);
			}
			deductQuote(quote);
		}
		
		if(!PriceUtils.isZero(order.getRemainingQty())) {
			order.setOrdStatus(OrdStatus.CANCELED);
			ackOrder(ExecType.CANCELED, order, null, "");
		}
		
	}
	
	private void fillLimitOrder(ChildOrder order, Quote quote, boolean late) {
		OrderSide side = order.getSide();
		List<QtyPrice> list = side.isBuy()?quote.getAsks():quote.getBids();
		//if no level 2 data
		if(null == list || list.size() == 0 || PriceUtils.isZero(list.get(0).getQuantity())) {
			QtyPrice qp = QuoteUtil.getLevelOneMatchingQtyPrice(quote, order.getPrice(), order.getRemainingQty(), order.getSide());
			if(!PriceUtils.isZero(qp.getPrice())) {
				double price = late?order.getPrice():qp.getPrice();
				fillOrderWithLevelOneLiquidity(order, price, qp.getQuantity(), quote);
			}
		} else { // work with level 2 data
			for(QtyPrice qp: list) {
				if(!QuoteUtil.priceCanMatch(qp.getPrice(), order.getPrice(), order.getSide()))
					break;
					
				double price = late?order.getPrice():qp.getPrice();
				double remainingQty = order.getRemainingQty();
				if(PriceUtils.EqualLessThan(remainingQty, qp.getQuantity())) {
					fillOrder(order, price, remainingQty);
					qp.setQuantity(qp.getQuantity() - remainingQty);
					break;
				}
					
				fillOrder(order, price, qp.getQuantity());
				qp.setQuantity(0);
			}
			deductQuote(quote);
		}
	}
	
	IDownStreamSender dsSender = new IDownStreamSender() {

		@Override
		public boolean getState() {
			return true;
		}

		@Override
		synchronized public void newOrder(ChildOrder order) throws DownStreamException {
			Quote quote = currentQuotes.get(order.getSymbol());
			if(null == quote)
				throw new DownStreamException("Quote isn't found for this symbol: " + order.getSymbol());

			if(order.getType().equals(ExchangeOrderType.MARKET)) {
				fillMarketOrder(order, quote);
			} else if (order.getType().equals(ExchangeOrderType.LIMIT)) {
				order.setOrdStatus(OrdStatus.NEW);
				ackOrder(ExecType.NEW, order, null, "");
				fillLimitOrder(order, quote, false);
				if(!PriceUtils.isZero(order.getRemainingQty())) {
					DualSet dualSet = exchangeOrders.get(quote.getSymbol());
					if(null == dualSet) {
						dualSet = new DualSet();
						exchangeOrders.put(quote.getSymbol(), dualSet);
					}
					dualSet.add(order);
				}
			} else {
				throw new DownStreamException("Order type not yet supported in back test: " + order.getType());
			}
			
			sendOrderAcks();
		}

		@Override
		synchronized public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			Quote quote = currentQuotes.get(order.getSymbol());
			DualSet dualSet = exchangeOrders.get(quote.getSymbol());
			if(null == dualSet) {
				dualSet = new DualSet();
				exchangeOrders.put(quote.getSymbol(), dualSet);
			}
		
			Double price = (Double)fields.get(OrderField.PRICE.value());
			boolean priceChanged = price != null && !PriceUtils.Equal(price, order.getPrice());
			ChildOrder found = null;
			Set<ChildOrder> orders = order.getSide().isBuy()?dualSet.bids:dualSet.asks;
			for(ChildOrder o: orders) {
				if(o.getId().equals(order.getId())) {
					found = o;
					break;
				}
			}
			
			if(null != found) {
				for(Entry<String, Object> entry: fields.entrySet()) {
					found.put(entry.getKey(), entry.getValue());
				}
				found.setOrdStatus(OrdStatus.REPLACED);
				ackOrder(ExecType.REPLACE, found, null, "");
				fillLimitOrder(found, quote, false);
				if(PriceUtils.isZero(found.getRemainingQty()))
					orders.remove(found);
				else if(priceChanged) { //price changed so re-insert to proper location
					orders.remove(found);
					orders.add(found); 
				}
			} else {
				ackOrder(ExecType.REJECTED, order, null, "Can't find this order");
			}
			sendOrderAcks();
		}

		@Override
		synchronized public void cancelOrder(ChildOrder order) throws DownStreamException {
			DualSet dualSet = exchangeOrders.get(order.getSymbol());
			if(null == dualSet) {
				dualSet = new DualSet();
				exchangeOrders.put(order.getSymbol(), dualSet);
			}
			Set<ChildOrder> orders = order.getSide().isBuy()?dualSet.bids:dualSet.asks;
			for(ChildOrder o: orders) {
				if(o.getId().equals(order.getId())) {
					o.setOrdStatus(OrdStatus.CANCELED);
					ackOrder(ExecType.CANCELED, o, null, "");
					sendOrderAcks();
					orders.remove(o);
					return;
				}
			}
			ackOrder(ExecType.REJECTED, order, null, "Can't find this order");
			sendOrderAcks();
		}
		
	};
	
	IDownStreamConnection dsCon = new  IDownStreamConnection() {

		@Override
		public void init() {
		}

		@Override
		public void uninit() {
		}

		@Override
		public String getId() {
			return "";
		}

		@Override
		public boolean getState() {
			return true;
		}

		@Override
		public IDownStreamSender setListener(IDownStreamListener listener) {
			dsListener = listener;
			return dsSender;
		}
		
	};
	
	@Override
	public List<IDownStreamConnection> getConnections() {
		List<IDownStreamConnection> list = new LinkedList<IDownStreamConnection>();
		list.add(dsCon);
		return list;
	}

	@Override
	public void init() {
		quotes = new PriorityQueue<Quote>(5, new Comparator<Quote>(){
			@Override
			public int compare(Quote o1, Quote o2) {
				long d1 = o1.getTimeStamp().getTime();
				long d2 = o2.getTimeStamp().getTime();
				
				if(d1 == d2)
					return 0;
				
				if(d1 > d2)
					return 1;
				
				return -1;
			}
		});
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getState() {
		return true;
	}

	@Override
	public void subscribeMarketData(String instrument,
			IMarketDataListener listener) throws MarketDataException {
		this.mdListener = listener; // supports only one listener
		Quote quote = currentQuotes.get(instrument);
		if(null != quote && null!= mdListener)
			mdListener.onQuote(quote);
	}

	@Override
	public void unsubscribeMarketData(String instrument,
			IMarketDataListener listener) {
		this.mdListener = null;
		
	}

	public ITickDataReader getTickDataReader() {
		return tickDataReader;
	}

	public void setTickDataReader(ITickDataReader tickDataReader) {
		this.tickDataReader = tickDataReader;
	}

}
