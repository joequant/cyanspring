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
package com.cyanspring.common.business;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.OrderUtils;

public class ParentOrderTest {

	@Test
	public void testBuySorting() {
		ParentOrder parentOrder = new ParentOrder("0005.HK", OrderSide.Buy, 2000, 0, OrderType.Market);
	
		Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();
		ChildOrder order;
		order = parentOrder.createChild(200, 50, ExchangeOrderType.LIMIT);
		childOrders.put(order.getId(), order);
		order = parentOrder.createChild(400, 49.5, ExchangeOrderType.LIMIT);
		childOrders.put(order.getId(), order);
		order = parentOrder.createChild(800, 50.5, ExchangeOrderType.LIMIT);
		childOrders.put(order.getId(), order);
		
		Set<ChildOrder> children = OrderUtils.getSortedOpenChildOrders(childOrders.values());
		ChildOrder prev = null;
		for(ChildOrder child: children) {
			if(prev == null) {
				prev = child;
			} else {
				assertTrue(prev.getPrice()>child.getPrice());
				prev = child;
			}
		}
	}
	
	@Test
	public void testSellSorting() {
		ParentOrder parentOrder = new ParentOrder("0005.HK", OrderSide.Sell, 2000, 0, OrderType.Market);
		
		Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();
		ChildOrder order;
		order = parentOrder.createChild(200, 50, ExchangeOrderType.LIMIT);
		childOrders.put(order.getId(), order);
		order = parentOrder.createChild(400, 49.5, ExchangeOrderType.LIMIT);
		childOrders.put(order.getId(), order);
		order = parentOrder.createChild(800, 50.5, ExchangeOrderType.LIMIT);
		childOrders.put(order.getId(), order);

		Set<ChildOrder> children = OrderUtils.getSortedOpenChildOrders(childOrders.values());
		ChildOrder prev = null;
		for(ChildOrder child: children) {
			if(prev == null) {
				prev = child;
			} else {
				assertTrue(prev.getPrice()<child.getPrice());
				prev = child;
			}
		}
	}
}
