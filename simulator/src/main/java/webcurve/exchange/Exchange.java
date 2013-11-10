package webcurve.exchange;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;



/**
 * This class implements a Stock exchange simulator. Orders entered into the simulator are like live
 * exchange, queued by time order and matched at the best price. It also provides callback management 
 * for updates like order book changes, individual order changes and trades generated.
 * @author dennis_d_chen@yahoo.com
 *
 */
public class Exchange {
    private static Logger log = LoggerFactory.getLogger(Exchange.class);
	

	String name;
	

	/**
	 * This property has no effect on the exchange running at the moment. It simply denotes the name.
	 * @return The name of exchange
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the exchange
	 * @param name name of the exchange
	 */
	public void setName(String name) {
		this.name = name;
	}
	Hashtable<String, OrderBook> books = new Hashtable<String, OrderBook>(); // store order by stock code
	Vector<Trade> trades = new Vector<Trade>();
	
	/**
	 * A generic class to manage various listener for update, please look into test2 sample
	 * program for the usages
	 * 
	 * @param <T>
	 */
	public class ListenerKeeper<T>
	{
		private Vector<ExchangeListener<T>> exchangeListeners = new Vector<ExchangeListener<T>>();
		
		public void addExchangeListener(ExchangeListener<T> listener)
		{
			if (null == listener)
				return;
			if (!exchangeListeners.contains(listener))
				exchangeListeners.add(listener);
		}
	
		public void removeExchangeListener(ExchangeListener<T> listener)
		{
			exchangeListeners.remove(listener);
		}
		
		public void updateExchangeListeners(T t)
		{
			for (ExchangeListener<T> item: exchangeListeners)
			{
				try
				{
					item.onChangeEvent(t);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * ListenerKeeper for OrderBook
	 */
	public final ListenerKeeper<OrderBook>	orderBookListenerKeeper = new ListenerKeeper<OrderBook>();
	/**
	 * ListenerKeeper for Order
	 */
	public final ListenerKeeper<Order>	orderListenerKeeper = new ListenerKeeper<Order>();
	/**
	 * ListenerKeeper for Trade
	 */
	public final ListenerKeeper<Trade>	tradeListenerKeeper = new ListenerKeeper<Trade>();

	private long tranIDSeed = 100;
	public synchronized long getNextTranID()
	{
		return ++tranIDSeed;
	}

	private long orderIDSeed = 1000;
	protected synchronized long getNextOrderID()
	{
		return ++orderIDSeed;
	}
	
	protected void touchOrder(Order order)
	{
		order.setAmendTime(new Date());
		order.setTranSeqNo(getNextTranID());
	}
	
	protected synchronized void addTrades(Vector<Trade> trades)
	{
		if (null==trades)
			return;
		
		for (int i=0; i<trades.size(); i++)
		{
			Trade trade = trades.get(i);
			trade.setTradeID(this.getNextOrderID());
			trade.setTranSeqNo(this.getNextTranID());
			this.trades.add(trade);
		}
	}
	
	/**
	 * This method retrieves the market depth by code
	 * @param code Stock code
	 * @return market depth
	 */
	public OrderBook getBook(String code)
	{
		OrderBook book = books.get(code);
		if (book == null)
		{
			book = new OrderBook(code);
			books.put(code, book);
		}
		return book;
	}
 
    // An new order can end up with 3 results
    // 1 - the full order in order book
    // 2 - partial traded, the remains are in order book
    // 3 - fully traded
		
	/**
	 * Enter an order into exchange.
	 * An new order can end up with 3 results
	 * <li>With full quantity queued in order book
	 * <li>Partially traded, the remaining quantity is queued in order book
	 * <li>Fully traded, no residual in order book
	 * <p>Order.status combines with quantity field can be checked to find out the order results.
	 * 
	 * @param code Stock code
	 * @param type only support 2 types at the moment: LIMIT or MARKET
	 * @param side Bid or ask(buy or sell)
	 * @param quantity Quantity of the order
	 * @param price price of the order, only valid for LIMIT type order
	 * @param broker broker of this order. This effectively denotes who enters this order.
	 * @param clientOrderID Order id assigned by user. Exchange simulator simply puts it in ClOrderID field of
	 * the order return
	 * @return The order entered.
	 */
	public Order enterOrder(String code, Order.TYPE type, Order.SIDE side, 
			int quantity, double price, String broker, String clientOrderID)
	{
		Order order = new Order(code, type, side, quantity, price, broker);
		order.setClOrderId(clientOrderID);

		if(quantity == 0 || (type != Order.TYPE.MARKET && price == 0.0)) {
			return null;
		}
		
		
		OrderBook book;		
		synchronized(books) {
			book = books.get(order.getCode());
			if ( book == null ) //first order
			{
				book = new OrderBook(order.getCode());
				books.put(order.getCode(), book);
			}
		}
		book.enterOrder(order, this, false);
		addTrades(trades);
		orderBookListenerKeeper.updateExchangeListeners(book);
        return order;
	}
	
	/**
	 * Cancel an order in the exchange
	 * @param orderID The order ID to be cancelled
	 * @param code The stock code of the order
	 * @param side the side of the order, bid or ask
	 * @param clientOrderID user can assign a new client order id for the cancel action.
	 * This is useful for supporting some interfaces such as FIX protocol
	 * @return true if successful, false if order can't be found.
	 */
	public boolean cancelOrder(long orderID, String code, Order.SIDE side, String clientOrderID)
	{
		OrderBook book = books.get(code);
		if ( book == null ) //first order
			return false;
		
		boolean result = (book.cancelOrder(orderID, side, this, clientOrderID) != null);

		orderBookListenerKeeper.updateExchangeListeners(book);
		return result;
	}
	
	/**
	 * Amend an order. Only quantity and price are allowed to be amend. Value passed in 0 means no change to this field.
	 * Amending up quantity is like entering a new order with the extra up quantity.
	 * 
	 * @param orderID The order ID to be amended
	 * @param code The stock code of the order
	 * @param side the side of the order, bid or ask
	 * @param quantity Quantity of the order amended to. Value of 0 indicates no change.
	 * @param price Price of the order amended to. Value of 0.0 indicates no change
	 * @param clientOrderID  clientOrderID user can assign a new client order id for the amend action.
	 * This is useful for supporting some interfaces such as FIX protocol.
	 * @return true if successful, false if amend failed.
	 */
	public boolean amendOrder(long orderID, String code, Order.SIDE side, 
			int quantity, double price, String clientOrderID)
	{
		OrderBook book = books.get(code);
		if ( book == null ) //first order
		{
			log.error("Order book is null: " + code);
			return false;
		}
		
		if (!book.amendOrder(orderID, side, quantity, price, clientOrderID, this))
			return false;
		addTrades(trades);
		orderBookListenerKeeper.updateExchangeListeners(book);
		return true;
	}	
	
	void orderUpdate(Order order, Order.STATUS status)
	{
		if(status != null)
			order.setStatus(status);
		Order updateOrder = (Order)order.clone();
        touchOrder(updateOrder);
        orderListenerKeeper.updateExchangeListeners(updateOrder);
	}
	
	/**
	 * removes all data from exchange inlcuding historical data
	 */
	public void reset() {
		for(OrderBook book: books.values()) {
			book.reset();
		}
		trades.clear();
	}
}
