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
package com.cyanspring.strategy.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.server.strategy.SingleOrderStrategyTest;

public class SinperBuyTest extends SingleOrderStrategyTest {

	@Override
	protected ParentOrder createData() {
		ParentOrder order = new ParentOrder("0005.HK", OrderSide.Buy, 200000, 68.20, OrderType.Limit);
		order.put(OrderField.STRATEGY.value(), "SNIPER");
		return order;
	}
	
	@Test
	public void test() {
		ParentOrder order = strategy.getParentOrder();
		// enter an order within sniper price limit
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, 10000, 68.20, "", "");
		timePass(10000);
		assertTrue(PriceUtils.Equal(order.getCumQty(), 10000));
		
		// enter two orders within sniper price limit
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, 10000, 68.20, "", "");
		
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, 20000, 68.15, "", "");
		timePass(10000);
		assertTrue(PriceUtils.Equal(order.getCumQty(), 40000));
		
		// enter an order out of sniper limit, no action
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, 10000, 68.25, "", "");
		
		timePass(10000);
		assertTrue(PriceUtils.Equal(order.getCumQty(), 40000));
	}

}
