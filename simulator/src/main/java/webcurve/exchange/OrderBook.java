package webcurve.exchange;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.util.PriceUtils;


/**
 * This class implements the back bone of the exchange simulator. 
 * It encapsulate the market depth object of each stock
 * @author dennis_d_chen@yahoo.com
 */
public class OrderBook implements Serializable, Cloneable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 3561053761150961403L;

	private static Logger log = LoggerFactory.getLogger(OrderBook.class);

	protected String code;
	protected List<Order> bidOrders = Collections.synchronizedList(new ArrayList<Order>());
	protected List<Order> askOrders = Collections.synchronizedList(new ArrayList<Order>());

	protected List<Trade> trades = Collections.synchronizedList(new ArrayList<Trade>());
	protected Double VWAP = 0.0;
	protected Double tradedVolume = 0.0;

	protected double high;
	protected double low;
	protected double open;
	protected double close;
	
	
	/**
	 * removes all data from this book inlcuding historical data
	 */
	synchronized public void reset() {
		bidOrders.clear();
		askOrders.clear();
		trades.clear();
		VWAP = 0.0;
		tradedVolume = 0.0;
		high = 0.0;
		low = 0.0;
		open = 0.0;
		close = 0.0;
	}
	
	
	public Double getTradedVolume() {
		return tradedVolume;
	}

	/**
	 * @return the last trade price
	 */	
	synchronized public double getLast()
	{
		if (trades.size() == 0)
			return 0.0;
		
		return trades.get(trades.size()-1).getPrice();
	}
	
	/**
	 * @return the last trade volume
	 */	
	synchronized public long getLastVol()
	{
		if (trades.size() == 0)
			return 0;
		
		return trades.get(trades.size()-1).getQuantity();
		
	}
	
	/**
	 * @return the VWAP of this stock since trading
	 */
	public Double getVWAP()
	{
		return VWAP;
	}
	
	/**
	 * @return the best bid price
	 */
	synchronized public double getBestBid()
	{
		if (bidOrders.size()>0)
			return bidOrders.get(0).getPrice();
		return 0.0;
	}
	

	/**
	 * @return the best ask price
	 */
	synchronized public double getBestAsk()
	{
		if (askOrders.size()>0)
			return askOrders.get(0).getPrice();
		return 0.0;
	}

	/**
	 * @return the best bid volume
	 */
	synchronized public long getBestBidVol()
	{
		if (bidOrders.size()==0)
			return 0;
		
		double price = bidOrders.get(0).getPrice();
		long total = 0;
		for (Order order : bidOrders)
		{
			if(new BigDecimal(price).equals(new BigDecimal(order.getPrice())))
			{
				total += order.getQuantity();
			}
			else
				break;
		}
		return total;
	}
	

	/**
	 * @return the best ask price
	 */
	synchronized public long getBestAskVol()
	{
		if (askOrders.size()==0)
			return 0;
		
		double price = askOrders.get(0).getPrice();
		long total = 0;
		for (Order order : askOrders)
		{
			if(new BigDecimal(price).equals(new BigDecimal(order.getPrice())))
			{
				total += order.getQuantity();
			}
			else
				break;
		}
		return total;
	}
	
	/**
	 * @param code Stock code
	 */
	public OrderBook(String code)
	{
		this.code = code;
	}
	
	/**
	 * @return returns the trades done
	 */
	public List<Trade> getTrades() {
		return trades;
	}
	
	/**
	 * @return returns the stock code of this market depth object
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return returns the buy order list of the order book.
	 */
	public List<Order> getBidOrders() {
		return bidOrders;
	}


	/**
	 * @return returns the buy orders of the order book per price level.
	 */
	public List<Order> getSumBidOrders() {
		return getSumOrders(bidOrders);
	}

	/**
	 * @return returns the sell list of the order book.
	 */
	public List<Order> getAskOrders() {
		return askOrders;
	}

	/**
	 * @return returns the Sell orders of the order book per price level.
	 */
	synchronized public List<Order> getSumAskOrders() {
		return getSumOrders(askOrders);
	}

	synchronized private List<Order> getSumOrders(List<Order> orders)
	{
		List<Order> sumOrders = new ArrayList<Order>();
		double price = 0.0;
		for(Order order: orders)
		{
			if (PriceUtils.Equal(order.getPrice(), price))
			{
				if (sumOrders.size()>0)
				{
					Order currOrder = sumOrders.get(sumOrders.size()-1);
					currOrder.setQuantity(currOrder.getQuantity() + order.getQuantity());
					currOrder.setBroker("");
				}
				else
					sumOrders.add((Order) order.clone());
			}
			else
			{
				sumOrders.add((Order) order.clone());
				price = order.getPrice();
			}
			
		}
		return sumOrders;	
	}
	
	private boolean compareOrders(Order order1, Order order2)
	{
		if (order1.getSide() == Order.SIDE.BID)
			return !(order1.getPrice() < order2.getPrice());
		else
			return !(order1.getPrice() > order2.getPrice());
	}

	synchronized protected void addTrade(Trade trade)
	{
		trades.add(trade);
		//recalculate VWAP
    	VWAP = VWAP * tradedVolume /(tradedVolume + trade.getQuantity())
				+ trade.getPrice() * trade.getQuantity() / (tradedVolume + trade.getQuantity());
    	tradedVolume += trade.getQuantity(); 
    	
    	if(trade.getPrice()> high)
    		high = trade.getPrice();
    	if(low == 0 || trade.getPrice() < low)
    		low = trade.getPrice();
	}
	
	private void tradeUpdate(Exchange exchange, Trade trade)
	{
		exchange.tradeListenerKeeper.updateExchangeListeners(trade);
	}
	
	protected synchronized void enterOrder (Order order, Exchange exchange, boolean isReenter)
	{
		if (isReenter) {
		    exchange.orderUpdate(order, Order.STATUS.AMENDED);
		}
		else {
			order.setOrderID(exchange.getNextOrderID());	
			exchange.orderUpdate(order, Order.STATUS.NEW);
		}
		
        // firstly search the opposite side for orders of matching price
        List<Order> oppositeOrders;
        if (order.getSide() == Order.SIDE.BID)
            oppositeOrders = askOrders;
        else
            oppositeOrders = bidOrders;

        boolean hasResidual = true;
        while (hasResidual && oppositeOrders.size() > 0)
        {
        	Order thisOrder = oppositeOrders.get(0);
            if (
            	order.getType() == Order.TYPE.MARKET ||
//                ((order.getSide() == Order.SIDE.BID) && (order.getPrice() >= thisOrder.getPrice())) ||
                ((order.getSide() == Order.SIDE.BID) && PriceUtils.EqualGreaterThan(order.getPrice(), thisOrder.getPrice())) ||
//                ((order.getSide() == Order.SIDE.ASK) && (order.getPrice() <= thisOrder.getPrice()))
                ((order.getSide() == Order.SIDE.ASK) && PriceUtils.EqualLessThan(order.getPrice(), thisOrder.getPrice()))
               )
            {
            	int tradeQty = 0;
                if (thisOrder.getQuantity() <= order.getQuantity())
                {
                	oppositeOrders.remove(0);
                	order.setQuantity(order.getQuantity() - thisOrder.getQuantity());
                	tradeQty = thisOrder.getQuantity();
                    order.calOrder(thisOrder.getQuantity(), thisOrder.getPrice());
                    thisOrder.calOrder(thisOrder.getQuantity(), thisOrder.getPrice());
                	thisOrder.setQuantity(0);
                    // updating order
                	exchange.orderUpdate(thisOrder, Order.STATUS.DONE);
                }
                else
                {
                	thisOrder.setQuantity(thisOrder.getQuantity() - order.getQuantity());                	
                	tradeQty = order.getQuantity();
                    order.calOrder(order.getQuantity(), thisOrder.getPrice());
                    thisOrder.calOrder(order.getQuantity(), thisOrder.getPrice());
                	order.setQuantity(0);
                    // updating order
                	exchange.orderUpdate(thisOrder, Order.STATUS.FILLING);
                }
                
                if(order.getQuantity() == 0)
                	exchange.orderUpdate(order, Order.STATUS.DONE);
                else
                	exchange.orderUpdate(order, Order.STATUS.FILLING);
                
            	Trade trade = new Trade(exchange.getNextOrderID(), tradeQty, thisOrder.getPrice(), 
						(Order)order.clone(), (Order)thisOrder.clone() );
				addTrade(trade);
				tradeUpdate(exchange, trade);
				
				if (order.getQuantity() <= 0)
				{
					hasResidual = false;
					break;
				} 
            }
            else // order is sorted by price/time, break right away if we cant find the matching opportunity
            {
            	break;
            }
        }

        if (hasResidual)
        {
        	if (order.getType() == Order.TYPE.LIMIT)
        		insertOrder(order);
        	else
        		exchange.orderUpdate(order, Order.STATUS.CANCELLED);
         }
	}
	
	protected synchronized void insertOrder(Order order)
	{
		List<Order> orders;
		if (order.getSide() == Order.SIDE.BID)
			orders = bidOrders;
		else
			orders = askOrders;
		
		
		int startIndex = 0;
		int endIndex = orders.size();

		//binary search and insert
		while(endIndex > startIndex)
		{
			int midIndex = (endIndex + startIndex)/2;
			if (compareOrders(orders.get(midIndex), order))
				startIndex = midIndex+1;
			else
				endIndex = midIndex;				
		}
		
		orders.add(endIndex, order);	
	}
	
	protected synchronized Order cancelOrder(long orderID, Order.SIDE side, Exchange exchange, String newClOrdId)
	{
		List<Order> orders;
		if (side == Order.SIDE.BID)
			orders = bidOrders;
		else
			orders = askOrders;

		for (int i=0; i<orders.size(); i++)
		{
			Order order = orders.get(i);
			if(order.getOrderID() == orderID)
			{
				if(newClOrdId != null && !newClOrdId.equals("")) {
					order.setOrigClOrderId(order.getClOrderId());
					order.setClOrderId(newClOrdId);
				}
				orders.remove(i);
				exchange.orderUpdate(order, Order.STATUS.CANCELLED);
				return order;
			}
		}
		log.warn("Cancel order not found: " + orderID);
		return null;
	}

	protected synchronized boolean amendOrder(long orderID, Order.SIDE side, 
			int quantity, double price, 
			String newClOrdId, 
			Exchange exchange)
	{
		if (0 == quantity && 0.0 == price)
			return false;

		List<Order> orders;
		if (side == Order.SIDE.BID)
			orders = bidOrders;
		else
			orders = askOrders;
		
		// find the order in depth
		int pos = 0;
		Order thisOrder = null;
		for (int i=0; i<orders.size(); i++)
		{
			if(orders.get(i).getOrderID() == orderID)
			{
				thisOrder = orders.get(i);
				pos = i;
				break;
			}
		}
		
		// return if not found
		if (null == thisOrder)
			return false;
		
		
		if (0.0 != price && price != thisOrder.getPrice()) //price has been changed
		{
			log.info("price amending: " + thisOrder.getPrice() + ", " + price);
			orders.remove(pos);
			thisOrder.setPrice(price);
			if ( 0 != quantity )
			{
				thisOrder.amendQuantity(quantity);
			}
			if(newClOrdId != null && !newClOrdId.equals("")) {
				thisOrder.setOrigClOrderId(thisOrder.getClOrderId());
				thisOrder.setClOrderId(newClOrdId);
				thisOrder.setPrevOrderID(thisOrder.getOrderID());
			}
			enterOrder(thisOrder, exchange, true);
			return true;
		}
		else if (0 != quantity && quantity != thisOrder.getQuantity()) //only quantity has been changed.
		{
			log.info("quantity amending: " + thisOrder.getQuantity() + " -> " + quantity);
			//check whether the order is the last order in the same price queue
			boolean lastOrderInQueue = true;
			if (pos < orders.size()-1)
			{
				Order nextOrder = orders.get(pos+1);
				if (nextOrder.getPrice() == thisOrder.getPrice())
					lastOrderInQueue = false;
			}
			
			if (lastOrderInQueue || (quantity < thisOrder.getQuantity()) ) // simple change the order quantity
			{
				thisOrder.amendQuantity(quantity);
				if(newClOrdId != null && !newClOrdId.equals("")) {
					thisOrder.setOrigClOrderId(thisOrder.getClOrderId());
					thisOrder.setClOrderId(newClOrdId);
				}
				exchange.orderUpdate(thisOrder, Order.STATUS.AMENDED);
			}
			else // create a new order if the order isn't the last order in the price queue
			{
				Order order = new Order(code, thisOrder.getType(), side, quantity-thisOrder.getQuantity(), 
						price, thisOrder.getBroker());
				order.setParentOrderID(thisOrder.getOrderID());
				if(newClOrdId != null && !newClOrdId.equals("")) {
					thisOrder.setOrigClOrderId(thisOrder.getClOrderId());
					thisOrder.setClOrderId(newClOrdId);
				} else {
					thisOrder.setClOrderId(newClOrdId);
				}
				enterOrder(order, exchange, false);
			}
			return true;
		}
		
		log.warn("Can't amend order: " + orderID + ", " + quantity + ", " + price + " : " + thisOrder.getQuantity() + ", " + thisOrder.getPrice());
		return false;
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

	public void show()
	{
		System.out.printf("%n                    %s                      %n", getCode());
		System.out.printf("--------------------------------------------%n");
		System.out.printf("         Buy                  Sell          %n");
		System.out.printf("--------------------------------------------%n");
		int i = 0;
		while(i< Math.max(getBidOrders().size(),getAskOrders().size()))
		{
			if (i < getBidOrders().size())
			{
				Order order = getBidOrders().get(i);
				System.out.printf("% 10d% 10.3f", order.getQuantity(), order.getPrice());
			}
			else
				System.out.printf("                    ");
			System.out.printf(" | ");
			if (i < getAskOrders().size())
			{
				Order order = getAskOrders().get(i);
				System.out.printf("%-10.3f% 10d", order.getPrice(), order.getQuantity());
			}
			System.out.printf("%n");
			i++;
		}
		System.out.printf("--------------------------------------------%n%n");
	}

}
