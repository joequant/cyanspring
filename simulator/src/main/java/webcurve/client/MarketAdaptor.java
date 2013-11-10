package webcurve.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;

/**
 * This class implements asynchronized updates for market data and orders.
 * @author dennis_d_chen@yahoo.com
 *
 */
public class MarketAdaptor implements Runnable{

	class ListenerKeeper<T> {
		HashMap<String, ArrayList<ExchangeListener<T>>> map 
			= new HashMap<String, ArrayList<ExchangeListener<T>>>();
		boolean subscribe(String stock, ExchangeListener<T> listener)
		{
			if (map.containsKey(stock))
			{
				ArrayList<ExchangeListener<T>> listeners = map.get(stock);
				synchronized(listeners)
				{
					if (listeners.contains(listener))
						return false;
					else
						listeners.add(listener);
				}
			}
			else
			{
				synchronized(map)
				{
					ArrayList<ExchangeListener<T>> listeners = new ArrayList<ExchangeListener<T>>();
					listeners.add(listener);
					map.put(stock, listeners);
				}
			}
			return true;
		}
		
		boolean unsubscribe(String stock, ExchangeListener<T> listener)
		{
			if (map.containsKey(stock))
			{
				synchronized(map)
				{
					ArrayList<ExchangeListener<T>> listeners = map.get(stock);
					if (listeners.contains(listener))
					{
						listeners.remove(listener);
						return true;
					}
					else
						return false;
				}
			}
			else
			{
				return false;
			}
		}
		
		ArrayList<ExchangeListener<T>> getListeners(String stock)
		{
			if (map.containsKey(stock))
			{
				return map.get(stock);
			}
			return null;
		}
	}

	ListenerKeeper<OrderBook> orderBookKeeper = new ListenerKeeper<OrderBook>();
	public boolean subscribeBook(String stock, ExchangeListener<OrderBook> listener)
	{
		return orderBookKeeper.subscribe(stock, listener);
	}
	
	public boolean unsubscribeBook(String stock, ExchangeListener<OrderBook> listener)
	{
		return orderBookKeeper.unsubscribe(stock, listener);
	}
	
	
	ListenerKeeper<Order> orderKeeper = new ListenerKeeper<Order>();
	public boolean subscribeOrder(String stock, ExchangeListener<Order> listener)
	{
		return orderKeeper.subscribe(stock, listener);
	}
	
	public boolean unsubscribeOrder(String stock, ExchangeListener<Order> listener)
	{
		return orderKeeper.unsubscribe(stock, listener);
	}
	
	ListenerKeeper<Trade> tradeKeeper = new ListenerKeeper<Trade>();
	public boolean subscribeTrade(String stock, ExchangeListener<Trade> listener)
	{
		return tradeKeeper.subscribe(stock, listener);
	}
	
	public boolean unsubscribeTrade(String stock, ExchangeListener<Trade> listener)
	{
		return tradeKeeper.unsubscribe(stock, listener);
	}
	
	@SuppressWarnings("rawtypes")
	LinkedList list = new LinkedList();

	@SuppressWarnings("unused")
	private Exchange exchange;
	private Thread thread;
	public MarketAdaptor(Exchange exchange)
	{
		this.exchange = exchange;
		exchange.orderBookListenerKeeper.addExchangeListener(new ExchangeListener<OrderBook>(){

			//@Override
			@SuppressWarnings("unchecked")
			public void onChangeEvent(OrderBook book) {
				synchronized(list)
				{
					list.add(book);
				}
				synchronized(MarketAdaptor.this)
				{
					MarketAdaptor.this.notify();
				}
			}
			
		});
		
		exchange.tradeListenerKeeper.addExchangeListener(new ExchangeListener<Trade>(){

			//@Override
			@SuppressWarnings("unchecked")
			public void onChangeEvent(Trade trade) {
				synchronized(list)
				{
					list.add(trade);
				}
				synchronized(MarketAdaptor.this)
				{
					MarketAdaptor.this.notify();
				}
			}
			
		});
		
		exchange.orderListenerKeeper.addExchangeListener(new ExchangeListener<Order>(){

			//@Override
			@SuppressWarnings("unchecked")
			public void onChangeEvent(Order order) {
				synchronized(list)
				{
					list.add(order);
				}
				synchronized(MarketAdaptor.this)
				{
					MarketAdaptor.this.notify();
				}
			}
			
		});
		
		thread = new Thread(this);  
		thread.start();
		

	}


	public void close()
	{
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private boolean running = true;
	public void run() {
		while(running)
		{
			synchronized(this)
			{
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			synchronized(list)
			{
				while(list.size()>0)
				{
					Object item = list.remove();
					
					if (item instanceof OrderBook)
					{
						OrderBook orderBook = (OrderBook)item;
						ArrayList<ExchangeListener<OrderBook>> listeners 
							= orderBookKeeper.getListeners(orderBook.getCode());
						for(ExchangeListener<OrderBook> listener: listeners)
						{
							listener.onChangeEvent(orderBook);
						}
					}
					else if (item instanceof Order)
					{
						Order order = (Order)item;
						ArrayList<ExchangeListener<Order>> listeners 
							= orderKeeper.getListeners(order.getCode());
						for(ExchangeListener<Order> listener: listeners)
						{
							listener.onChangeEvent(order);
						}						
					}
					else if (item instanceof Trade)
					{
						Trade trade = (Trade)item;
						ArrayList<ExchangeListener<Trade>> listeners 
							= tradeKeeper.getListeners(trade.getAskOrder().getCode());
						for(ExchangeListener<Trade> listener: listeners)
						{
							listener.onChangeEvent(trade);
						}						
					}
					else
					{
						System.out.println("I don't expect to see this");
					}
					
				}
			}


		}
		
	}
	

	
}
