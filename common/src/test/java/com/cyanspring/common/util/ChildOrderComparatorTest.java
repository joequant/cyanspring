package com.cyanspring.common.util;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;

public class ChildOrderComparatorTest {

	@Test
	public void testSell() throws InterruptedException {
		Set<ChildOrder> orders = new TreeSet<ChildOrder>(OrderUtils.childOrderComparator);
		 ChildOrder order1 = new ChildOrder("0005.HK", OrderSide.Sell, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order2 = new ChildOrder("0005.HK", OrderSide.Sell, 80000, 68.5, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order3 = new ChildOrder("0005.HK", OrderSide.Sell, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order4 = new ChildOrder("0005.HK", OrderSide.Sell, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		
		 Thread.sleep(1);
		 ChildOrder order5 = new ChildOrder("0005.HK", OrderSide.Sell, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order6 = new ChildOrder("0005.HK", OrderSide.Sell, 80000, 68.3, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 orders.add(order2);
		 orders.add(order4);
		 orders.add(order5);
		 orders.add(order1);
		 orders.add(order3);
		 orders.add(order6);
		 
		 ChildOrder prev = null;
		 System.out.println("--- Sell ---");
		 for(ChildOrder order: orders) {
			 System.out.println(order.getPrice() + " : " + order.getCreated().getTime());
			 if(null != prev) {
				 if(PriceUtils.Equal(prev.getPrice(), order.getPrice()))
					 assert(order.getCreated().getTime() >= prev.getCreated().getTime());
				 else
					 assert(prev.getPrice() < order.getPrice());
			 }
		 }
	}

	@Test
	public void testBuy() throws InterruptedException {
		Set<ChildOrder> orders = new TreeSet<ChildOrder>(OrderUtils.childOrderComparator);
		 ChildOrder order1 = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order2 = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.5, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order3 = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order4 = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		
		 Thread.sleep(1);
		 ChildOrder order5 = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.4, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 Thread.sleep(1);
		 ChildOrder order6 = new ChildOrder("0005.HK", OrderSide.Buy, 80000, 68.3, ExchangeOrderType.LIMIT,
				 "", "");
		 
		 orders.add(order5);
		 orders.add(order1);
		 orders.add(order2);
		 orders.add(order4);
		 orders.add(order3);
		 orders.add(order6);
		 
		 ChildOrder prev = null;
		 System.out.println("--- Buy ---");
		 for(ChildOrder order: orders) {
			 System.out.println(order.getPrice() + " : " + order.getCreated().getTime());
			 if(null != prev) {
				 if(PriceUtils.Equal(prev.getPrice(), order.getPrice()))
					 assert(order.getCreated().getTime() >= prev.getCreated().getTime());
				 else
					 assert(prev.getPrice() > order.getPrice());
			 }
		 }
	}
	
}
