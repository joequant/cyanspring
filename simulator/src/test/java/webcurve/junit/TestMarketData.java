package webcurve.junit;

import junit.framework.TestCase;
import webcurve.common.BaseOrder;
import webcurve.common.ExchangeListener;
import webcurve.common.Order;
import webcurve.common.Trade;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;

public class TestMarketData extends TestCase {
	Exchange exchange = new Exchange();
	String symbol = "0005.HK";
	OrderBook book;
	Trade trade;
	
	public TestMarketData(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		exchange.orderBookListenerKeeper.addExchangeListener(new ExchangeListener<OrderBook>(){

			//@Override
			public void onChangeEvent(OrderBook book) {
				TestMarketData.this.book = book;
			}
			
		});
		
		exchange.tradeListenerKeeper.addExchangeListener(new ExchangeListener<Trade>(){

			//@Override
			public void onChangeEvent(Trade trade) {
				TestMarketData.this.trade = trade;
			}
			
		});		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testOrderBook()
	{
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		assertEquals(2000, book.getBestBidVol());
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 4000, 86, "broker1", "CO-00000001");
		assertEquals(2000, book.getBestBidVol());
		assertEquals(4000, book.getBestAskVol());
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 1000, 86, "broker1", "CO-00000001");
		assertEquals(1000, trade.getQuantity());
	}
}
