package webcurve.test;

import org.junit.Ignore;

import webcurve.client.MarketAdaptor;
import webcurve.common.BaseOrder;
import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;

@Ignore
public class Test5 {
	Exchange exchange;
	MarketAdaptor adaptor;
	public Test5(Exchange exchange)
	{
		this.exchange = exchange;
		this.adaptor = new MarketAdaptor(exchange);
		adaptor.subscribeBook("0005.HK", new ExchangeListener<OrderBook>(){

			//@Override
			public void onChangeEvent(OrderBook book) {
				Test1.showBook(book);
			}
			
		});
		
		adaptor.subscribeTrade("0005.HK", new ExchangeListener<Trade>(){

			//@Override
			public void onChangeEvent(Trade trade) {
				Test2.showTrade(trade);
			}
			
		});
		
		adaptor.subscribeOrder("0005.HK", new ExchangeListener<Order>(){

			//@Override
			public void onChangeEvent(Order order) {
				Test2.showOrder(order);
			}
			
		});
	}
	
	public static void showTrade(Trade trade)
	{
		System.out.printf("==>Received trade(id %d) update: %d, %.3f%n", 
				trade.getTradeID(), trade.getQuantity(), trade.getPrice());
	}
	
	public static void showOrder(Order order)
	{
		System.out.printf("==>Received Order(id %d) update: %s, %d, %.3f%n", 
				order.getOrderID(),
				BaseOrder.sideToString(order.getSide()), 
				order.getQuantity(), order.getPrice());
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Exchange exchange = new Exchange();
		Test5 test = new Test5(exchange);
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1200, 68.60, "8675", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1200, 68.50, "8675", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 3200, 68.30, "7689", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 0600, 68.60, "7689", "");
		System.out.println("closing...");
		test.adaptor.close();
		System.out.println("closed");
	}

}
