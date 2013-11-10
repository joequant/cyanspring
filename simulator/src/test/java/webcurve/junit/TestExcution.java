package webcurve.junit;

import junit.framework.TestCase;
import webcurve.common.BaseOrder;
import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.exchange.Exchange;

public class TestExcution extends TestCase {
	Exchange exchange = new Exchange();
	String symbol = "0005.HK";
	Order order;
	Trade trade;
	
	public TestExcution(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testOrder()
	{
		exchange.orderListenerKeeper.addExchangeListener(new ExchangeListener<Order>(){

			//@Override
			public void onChangeEvent(Order order) {
				TestExcution.this.order = order;
			}
			
		});
		
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		assertEquals(2000, order.getQuantity());
		assertEquals(85.0, order.getPrice());
	}
	
	public void testTrade()
	{
		exchange.tradeListenerKeeper.addExchangeListener(new ExchangeListener<Trade>(){

			//@Override
			public void onChangeEvent(Trade trade) {
				TestExcution.this.trade = trade;
			}
			
		});
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 4000, 85, "broker1", "CO-00000001");
		assertEquals(2000, trade.getQuantity());
		assertEquals(85.0, trade.getPrice());
	}
	
	public void testCancel()
	{
		exchange.orderListenerKeeper.addExchangeListener(new ExchangeListener<Order>(){

			//@Override
			public void onChangeEvent(Order order) {
				TestExcution.this.order = order;
			}
			
		});
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		assertEquals(2000, order.getQuantity());
		assertEquals(85.0, order.getPrice());
		assertEquals(Order.STATUS.NEW, order.getStatus());
		exchange.cancelOrder(order.getOrderID(), order.getCode(), order.getSide(), "CO-00000002");
		assertEquals(Order.STATUS.CANCELLED, order.getStatus());

	}
	
	public void testAmend()
	{
		exchange.orderListenerKeeper.addExchangeListener(new ExchangeListener<Order>(){

			//@Override
			public void onChangeEvent(Order order) {
				TestExcution.this.order = order;
			}
			
		});
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		assertEquals(2000, order.getQuantity());
		assertEquals(85.0, order.getPrice());
		assertEquals(Order.STATUS.NEW, order.getStatus());
		exchange.amendOrder(order.getOrderID(), order.getCode(), order.getSide(), 0, 84.0, "CO-00000002");
		assertEquals(84.0, order.getPrice());
		assertEquals(Order.STATUS.AMENDED, order.getStatus());
		exchange.amendOrder(order.getOrderID(), order.getCode(), order.getSide(), 1000, 0, "CO-00000003");
		assertEquals(1000, order.getQuantity());
		assertEquals(Order.STATUS.AMENDED, order.getStatus());

	}
}
