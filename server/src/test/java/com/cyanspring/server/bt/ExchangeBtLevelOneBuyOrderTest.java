package com.cyanspring.server.bt;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Test;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.QtyPrice;

public class ExchangeBtLevelOneBuyOrderTest extends ExchangeBtOrderTest {
	@Test
	public void testFullyFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.2);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 2000, 68.3, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.FILLED));
		 assertTrue(orderAck.order.getCumQty() == order.getQuantity());
		 assertTrue(orderAck.order.getAvgPx() == 68.2);
		 
		 assertTrue(PriceUtils.Equal(quote.getAskVol(), 38000));
	}

	@Test
	public void testPartiallyFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 double askVol = quote.getAskVol();
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.3, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);

		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getCumQty() == askVol);

		 assertTrue(PriceUtils.Equal(quote.getAskVol(), 0));
	}

	@Test
	public void testLatePartiallyFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.1);
		 quote.setAskVol(40000);		
		 double askVol = quote.getAskVol();
		 exchange.setQuote(quote);

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getCumQty() == askVol);
		 assertTrue(orderAck.order.getAvgPx() == 68.2);
	}

	@Test
	public void testLateFullyFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.2);
		 quote.setAskVol(120000);		
		 exchange.setQuote(quote);

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.FILLED));
		 assertTrue(orderAck.order.getCumQty() == order.getQuantity());

	}

	@Test
	public void testAmendOrder() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 Map<String, Object> changes = new HashMap<String, Object>();
		 changes.put(OrderField.QUANTITY.value(), 2000.0);
		 changes.put(OrderField.PRICE.value(), 68.1);
		 sender.amendOrder(order, changes);
		 
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.REPLACE));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.REPLACED));
		 assertTrue(2000.0 == orderAck.order.getQuantity());
		 assertTrue(68.1 == orderAck.order.getPrice());

	}

	@Test
	public void testAmendOrderFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 Map<String, Object> changes = new HashMap<String, Object>();
		 changes.put(OrderField.QUANTITY.value(), 2000.0);
		 changes.put(OrderField.PRICE.value(), 68.3);
		 sender.amendOrder(order, changes);
		 
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.REPLACE));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.REPLACED));
		 assertTrue(2000.0 == orderAck.order.getQuantity());
		 assertTrue(68.3 == orderAck.order.getPrice());

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.FILLED));
		 assertTrue(orderAck.order.getCumQty() == orderAck.order.getQuantity());
	}
	
	@Test
	public void testAmendOrderNoFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 Map<String, Object> changes = new HashMap<String, Object>();
		 changes.put(OrderField.PRICE.value(), 68.1);
		 sender.amendOrder(order, changes);
		 
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.REPLACE));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.REPLACED));
		 assertTrue(68.1 == orderAck.order.getPrice());

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck == null);
	}
	
	@Test
	public void testAmendOrderPartiallyFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 double askVol = quote.getAskVol();
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 Map<String, Object> changes = new HashMap<String, Object>();
		 changes.put(OrderField.PRICE.value(), 68.3);
		 sender.amendOrder(order, changes);
		 
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.REPLACE));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.REPLACED));
		 assertTrue(68.3 == orderAck.order.getPrice());

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getCumQty() == askVol);
		 
		 new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.FILLED));
		 assertTrue(orderAck.order.getCumQty() == orderAck.order.getQuantity());
	}

	@Test
	public void testMultipleOrderFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 order = new ChildOrder("0005.HK", OrderSide.Buy, 40000, 68.25, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getCumQty() == 0);
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 
		 quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.2);
		 quote.setAskVol(100000);
		 exchange.setQuote(quote);
		 
		 
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.FILLED));
		 assertTrue(orderAck.order.getAvgPx() == 68.25);
		 assertTrue(orderAck.order.getCumQty() == 40000);
		 assertTrue(orderAck.order.getQuantity() == 40000);

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getAvgPx() == 68.2);
		 assertTrue(orderAck.order.getCumQty() == 60000);
		 assertTrue(orderAck.order.getQuantity() == 80000);
	}

	@Test
	public void testCancelOrder() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 sender.cancelOrder(order);
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.CANCELED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.CANCELED));
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 assertTrue(orderAck.order.getCumQty() == 0);
	}
	
	@Test
	public void testCancelOrderReject() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 assertTrue(orderAck.order.getCumQty() == 0);
		 
		 order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.2, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 sender.cancelOrder(order);
		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.REJECTED));
	}
	
	@Test
	public void testMarketOrderPartiallyFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 double askVol = quote.getAskVol();
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 0, ExchangeOrderType.MARKET,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 assertTrue(orderAck.order.getCumQty() == 0);

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.PARTIALLY_FILLED));
		 assertTrue(orderAck.order.getCumQty() == askVol);

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.CANCELED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.CANCELED));
	}
	
	@Test
	public void testMarketOrderFullyFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(68.3);
		 quote.setAskVol(40000);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 20000, 0, ExchangeOrderType.MARKET,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 assertTrue(orderAck.order.getCumQty() == 0);

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.FILLED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.FILLED));
		 assertTrue(orderAck.order.getCumQty() == orderAck.order.getQuantity());

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck == null);
	}
	
	@Test
	public void testMarketOrderNoFilled() throws DownStreamException {
		 Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		 quote.setAsk(0);
		 quote.setAskVol(0);
		 exchange.setQuote(quote);
		 ChildOrder order = new ChildOrder("0005.HK", OrderSide.Buy, 20000, 0, ExchangeOrderType.MARKET,
				 "", "");
		 sender.newOrder(order);
		 
		 OrderAck orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.NEW));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.NEW));
		 assertTrue(orderAck.order.getQuantity() == order.getQuantity());
		 assertTrue(orderAck.order.getCumQty() == 0);

		 orderAck = popFirstOrderAck();
		 assertTrue(orderAck.execType.equals(ExecType.CANCELED));
		 assertTrue(orderAck.order.getOrdStatus().equals(OrdStatus.CANCELED));
		 assertTrue(orderAck.order.getCumQty() == 0);

	}
	
	
}
