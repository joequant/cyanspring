/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.server.strategy;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.OrderUtils;

public class AdjQuoteTest {
	
	@Test
	public void testBuy() {
		ParentOrder parentOrder = new ParentOrder("0005.HK", OrderSide.Buy, 20000, 50, OrderType.Limit);
		
		ChildOrder child;
		Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();
		child = parentOrder.createChild(1200, 50, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(200, 47, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(400, 49.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(800, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(400, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		
		List<QtyPrice> bids = new LinkedList<QtyPrice>();
		List<QtyPrice> asks = new LinkedList<QtyPrice>();
		
		bids.add(new QtyPrice(3000, 50));
		bids.add(new QtyPrice(4000, 49.5));
		bids.add(new QtyPrice(5000, 49));
		bids.add(new QtyPrice(6000, 48.5));
		bids.add(new QtyPrice(7000, 48));
		Quote quote = new Quote("0005.HK", bids, asks);
		
		Quote adjQuote = OrderUtils.calAdjustedQuote(quote, OrderUtils.getSortedOpenChildOrders(childOrders.values()));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(0).quantity, 1800));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(1).quantity, 3600));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(2).quantity, 5000));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(3).quantity, 4800));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(4).quantity, 7000));
		assertTrue(PriceUtils.Equal(adjQuote.getBidVol(), 1800));
	}
	
	@Test
	public void testBuyShiftUp() {
		ParentOrder parentOrder = new ParentOrder("0005.HK", OrderSide.Buy, 20000, 50, OrderType.Limit);
		
		ChildOrder child;
		Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();
		child = parentOrder.createChild(3000, 50, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(200, 47, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(4000, 49.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(800, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(400, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		
		List<QtyPrice> bids = new LinkedList<QtyPrice>();
		List<QtyPrice> asks = new LinkedList<QtyPrice>();
		
		bids.add(new QtyPrice(3000, 50));
		bids.add(new QtyPrice(4000, 49.5));
		bids.add(new QtyPrice(5000, 49));
		bids.add(new QtyPrice(6000, 48.5));
		bids.add(new QtyPrice(7000, 48));
		Quote quote = new Quote("0005.HK", bids, asks);
		
		Quote adjQuote = OrderUtils.calAdjustedQuote(quote, OrderUtils.getSortedOpenChildOrders(childOrders.values()));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(0).quantity, 5000));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(1).quantity, 4800));
		assertTrue(PriceUtils.Equal(adjQuote.getBids().get(2).quantity, 7000));
		assertTrue(PriceUtils.Equal(adjQuote.getBidVol(), 5000));
		assertTrue(PriceUtils.Equal(adjQuote.getBid(), 49));
	}
	
	@Test
	public void testSell() {
		ParentOrder parentOrder = new ParentOrder("0005.HK", OrderSide.Sell, 20000, 50, OrderType.Limit);
		
		ChildOrder child;
		Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();
		child = parentOrder.createChild(200, 50, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(400, 49.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(800, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(400, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);

		List<QtyPrice> bids = new LinkedList<QtyPrice>();
		List<QtyPrice> asks = new LinkedList<QtyPrice>();
		
		asks.add(new QtyPrice(6000, 48.5));
		asks.add(new QtyPrice(5000, 49));
		asks.add(new QtyPrice(4000, 49.5));
		asks.add(new QtyPrice(3000, 50));
		Quote quote = new Quote("0005.HK", bids, asks);
		
		Quote adjQuote = OrderUtils.calAdjustedQuote(quote, OrderUtils.getSortedOpenChildOrders(childOrders.values()));
		assertTrue(PriceUtils.Equal(adjQuote.getAsks().get(0).quantity, 4800));
		assertTrue(PriceUtils.Equal(adjQuote.getAsks().get(1).quantity, 5000));
		assertTrue(PriceUtils.Equal(adjQuote.getAsks().get(2).quantity, 3600));
		assertTrue(PriceUtils.Equal(adjQuote.getAsks().get(3).quantity, 2800));
		
		assertTrue(PriceUtils.Equal(adjQuote.getAskVol(), 4800));
	}
	
	@Test
	public void testSellShiftUp() {
		ParentOrder parentOrder = new ParentOrder("0005.HK", OrderSide.Sell, 20000, 50, OrderType.Limit);
		
		ChildOrder child;
		Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();
		child = parentOrder.createChild(200, 50, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(400, 49, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(800, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);
		child = parentOrder.createChild(5200, 48.5, ExchangeOrderType.LIMIT);
		childOrders.put(child.getId(), child);

		List<QtyPrice> bids = new LinkedList<QtyPrice>();
		List<QtyPrice> asks = new LinkedList<QtyPrice>();
		
		asks.add(new QtyPrice(6000, 48.5));
		asks.add(new QtyPrice(5000, 49));
		asks.add(new QtyPrice(4000, 49.5));
		asks.add(new QtyPrice(3000, 50));
		Quote quote = new Quote("0005.HK", bids, asks);
		
		Quote adjQuote = OrderUtils.calAdjustedQuote(quote, OrderUtils.getSortedOpenChildOrders(childOrders.values()));
		assertTrue(PriceUtils.Equal(adjQuote.getAsks().get(0).quantity, 4600));
		assertTrue(PriceUtils.Equal(adjQuote.getAsks().get(1).quantity, 4000));
		assertTrue(PriceUtils.Equal(adjQuote.getAsks().get(2).quantity, 2800));
		
		assertTrue(PriceUtils.Equal(adjQuote.getAskVol(), 4600));
		assertTrue(PriceUtils.Equal(adjQuote.getAsk(), 49));
	}
}
