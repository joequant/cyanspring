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

public class PovBuyTest extends SingleOrderStrategyTest {

	@Override
	protected ParentOrder createData() {
		ParentOrder order = new ParentOrder("0005.HK", OrderSide.Buy, 200000, 68.30, OrderType.Limit);
		order.put(OrderField.STRATEGY.value(), "POV");
		order.put(OrderField.POV.value(), new Double(30));
		return order;
	}

	@Test
	public void test() {
		ParentOrder order = strategy.getParentOrder();
		timePass(10000);
		assertChildOrder(BID, 8400, true);
		
		// produce some market volume by hitting the ask
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.BID, 10000, 68.30, "", "");
		timePass(10000);
		assertChildOrder(BID, 8400, true);
		assertTrue(PriceUtils.Equal(order.getCumQty(), 4000));
		
		// produce some market volume by hitting the bid
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, 10000, 68.10, "", "");
		timePass(10000);
		assertChildOrder(BID, 4000, true);
		assertTrue(PriceUtils.Equal(order.getCumQty(), 8400));
	}
}
