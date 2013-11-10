package webcurve.junit;

import junit.framework.TestCase;
import webcurve.common.BaseOrder;
import webcurve.common.Order;
import webcurve.exchange.Exchange;
import webcurve.exchange.OrderBook;

public class TestOrders extends TestCase {
	
	Exchange exchange = new Exchange();
	String symbol = "0005.HK";
	
	public TestOrders(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testOrderEnter()
	{
		exchange = new Exchange();
		// test buy
		int quantity = 2000;
		double price = 85;
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, quantity, price, "broker1", "CO-00000001");
		OrderBook book = exchange.getBook(symbol);
		assertEquals(quantity, book.getBestBidVol());
		assertEquals(price, book.getBestBid());
		assertEquals(quantity, book.getBidOrders().get(0).getQuantity());
		assertEquals(price, book.getBidOrders().get(0).getPrice());
		
		
		int quantity1 = 4000;
		double price1 = price + 1;
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, quantity1, price1, "broker1", "CO-00000002");
		assertEquals(quantity1, book.getBestBidVol());
		assertEquals(price1, book.getBestBid());
		assertEquals(quantity1, book.getBidOrders().get(0).getQuantity());
		assertEquals(price1, book.getBidOrders().get(0).getPrice());
		assertEquals(quantity, book.getBidOrders().get(1).getQuantity());
		assertEquals(price, book.getBidOrders().get(1).getPrice());
		
		// test sell
		int quantity2 = 6000;
		double price2 = price1 + 1;
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, quantity2, price2, "broker1", "CO-00000001");
		assertEquals(quantity2, book.getBestAskVol());
		assertEquals(price2, book.getBestAsk());
		assertEquals(quantity2, book.getAskOrders().get(0).getQuantity());
		assertEquals(price2, book.getAskOrders().get(0).getPrice());
		
		// test sell
		int quantity3 = 8000;
		double price3 = price2 + 1;
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, quantity3, price3, "broker1", "CO-00000001");
		assertEquals(quantity2, book.getBestAskVol());
		assertEquals(price2, book.getBestAsk());
		assertEquals(quantity2, book.getAskOrders().get(0).getQuantity());
		assertEquals(price2, book.getAskOrders().get(0).getPrice());
		assertEquals(quantity3, book.getAskOrders().get(1).getQuantity());
		assertEquals(price3, book.getAskOrders().get(1).getPrice());

	}

	public void testBuyOrderPriority()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 3000, 84, "broker1", "CO-00000002");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 4000, 85, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 5000, 84.5, "broker1", "CO-00000003");
		assertEquals(2000, book.getBidOrders().get(0).getQuantity());
		assertEquals(4000, book.getBidOrders().get(1).getQuantity());
		assertEquals(5000, book.getBidOrders().get(2).getQuantity());
		assertEquals(3000, book.getBidOrders().get(3).getQuantity());
	}
	
	public void testSellOrderPriority()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 2000, 85, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 3000, 84, "broker1", "CO-00000002");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 4000, 85, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 5000, 84.5, "broker1", "CO-00000003");
		assertEquals(3000, book.getAskOrders().get(0).getQuantity());
		assertEquals(5000, book.getAskOrders().get(1).getQuantity());
		assertEquals(2000, book.getAskOrders().get(2).getQuantity());
		assertEquals(4000, book.getAskOrders().get(3).getQuantity());
		
		
	}
	
	public void testOrderMatching()
	{
		exchange = new Exchange();

		int quantity = 2000;
		double price = 85;
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, quantity, price, "broker1", "CO-00000001");
		OrderBook book = exchange.getBook(symbol);
		assertEquals(quantity, book.getBestBidVol());
		assertEquals(price, book.getBestBid());
		assertEquals(quantity, book.getBidOrders().get(0).getQuantity());
		assertEquals(price, book.getBidOrders().get(0).getPrice());
		
		int quantity2 = 6000;
		double price2 = price;
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, quantity2, price2, "broker1", "CO-00000001");
		assertEquals(quantity2 - quantity, book.getBestAskVol());

		// check trades
		assertEquals(quantity, book.getLastVol());
		assertEquals(price, book.getLast());
	}
	
	public void testBuyOrderSummaryBook()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 3000, 84, "broker1", "CO-00000002");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 4000, 85, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 5000, 84.5, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 6000, 84.5, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 7000, 84.5, "broker1", "CO-00000003");
		assertEquals(6000, book.getSumBidOrders().get(0).getQuantity());
		assertEquals(18000, book.getSumBidOrders().get(1).getQuantity());
		assertEquals(3000, book.getSumBidOrders().get(2).getQuantity());
	}

	public void testSellOrderSummaryBook()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 2000, 85, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 3000, 84, "broker1", "CO-00000002");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 4000, 85, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 5000, 84.5, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 6000, 84.5, "broker1", "CO-00000003");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 7000, 84.5, "broker1", "CO-00000003");
		assertEquals(3000, book.getSumAskOrders().get(0).getQuantity());
		assertEquals(18000, book.getSumAskOrders().get(1).getQuantity());
		assertEquals(6000, book.getSumAskOrders().get(2).getQuantity());
	}
	
	public void testBuyAmendOrder()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 10000, 86, "broker1", "CO-00000001");
		Order order = exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 8000, 85, "broker1", "CO-00000001");
		exchange.amendOrder(order.getOrderID(), order.getCode(), order.getSide(), 6000, 85, "CO-00000001");
		assertEquals(6000, book.getBidOrders().get(1).getQuantity());
		
		exchange.amendOrder(order.getOrderID(), order.getCode(), order.getSide(), 0, 87, "CO-00000001");
		assertEquals(6000, book.getBidOrders().get(0).getQuantity());
		assertEquals(87.0, book.getBidOrders().get(0).getPrice());
		assertEquals(10000, book.getBidOrders().get(1).getQuantity());
	}

	public void testSellAmendOrder()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 10000, 86, "broker1", "CO-00000001");
		Order order = exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 8000, 85, "broker1", "CO-00000001");
		exchange.amendOrder(order.getOrderID(), order.getCode(), order.getSide(), 6000, 85, "CO-00000001");
		assertEquals(6000, book.getAskOrders().get(0).getQuantity());
		
		exchange.amendOrder(order.getOrderID(), order.getCode(), order.getSide(), 0, 87, "CO-00000001");
		assertEquals(6000, book.getAskOrders().get(1).getQuantity());
		assertEquals(87.0, book.getAskOrders().get(1).getPrice());
		assertEquals(10000, book.getAskOrders().get(0).getQuantity());
	}
	
	public void testBuyCancelOrder()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 10000, 86, "broker1", "CO-00000001");
		Order order = exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 8000, 85, "broker1", "CO-00000001");
		assertEquals(2, book.getBidOrders().size());
		exchange.cancelOrder(order.getOrderID(), order.getCode(), order.getSide(), "CO-00000001");
		assertEquals(1, book.getBidOrders().size());
		
	}
	
	public void testSellCancelOrder()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 10000, 86, "broker1", "CO-00000001");
		Order order = exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 8000, 85, "broker1", "CO-00000001");
		assertEquals(2, book.getAskOrders().size());
		exchange.cancelOrder(order.getOrderID(), order.getCode(), order.getSide(), "CO-00000001");
		assertEquals(1, book.getAskOrders().size());
		
	}
	
	
	public void testBuyMatchManySell()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);

		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 2000, 85, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 3000, 86, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 4000, 86, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 8000, 87, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 12000, 88, "broker1", "CO-00000001");
		assertEquals(5000, book.getAskOrders().get(0).getQuantity());
	}
	
	public void testSellMatchManySell()
	{
		exchange = new Exchange();
		OrderBook book = exchange.getBook(symbol);

		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 2000, 85, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 3000, 86, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 4000, 86, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.BID, 8000, 87, "broker1", "CO-00000001");
		exchange.enterOrder(symbol, Order.TYPE.LIMIT, BaseOrder.SIDE.ASK, 12000, 81, "broker1", "CO-00000001");
		assertEquals(3000, book.getBidOrders().get(0).getQuantity());
		assertEquals(2000, book.getBidOrders().get(1).getQuantity());		
	}	
}
