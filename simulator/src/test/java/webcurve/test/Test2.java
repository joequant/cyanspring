package webcurve.test;
import org.junit.Ignore;

import webcurve.common.BaseOrder;
import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;
/**
 * @author dennis_d_chen@yahoo.com
 */
@Ignore
public class Test2 {
	
	Exchange exchange;
	public Test2(Exchange exchange)
	{
		this.exchange = exchange;
		exchange.orderBookListenerKeeper.addExchangeListener(new ExchangeListener<OrderBook>(){

			//@Override
			public void onChangeEvent(OrderBook book) {
				Test1.showBook(book);
			}
			
		});
		
		exchange.tradeListenerKeeper.addExchangeListener(new ExchangeListener<Trade>(){

			//@Override
			public void onChangeEvent(Trade trade) {
				Test2.showTrade(trade);
			}
			
		});
		
		exchange.orderListenerKeeper.addExchangeListener(new ExchangeListener<Order>(){

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
		Test2 test = new Test2(exchange);
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 3200, 68.50, "7689", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1200, 68.60, "8675", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1200, 68.50, "8675", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 3600, 68.40, "7689", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 1600, 68.70, "8675", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.BID, 4000, 68.30, "7689", "");
		exchange.enterOrder("0005.HK", Order.TYPE.LIMIT, Order.SIDE.ASK, 2000, 68.80, "8675", "");
		test.toString();
	}

}
