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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.order.AmendStrategyOrderEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.server.strategy.SingleOrderStrategyTest;

public class IcebergSellTest extends SingleOrderStrategyTest {

	@Override
	protected ParentOrder createData() {
		ParentOrder order = new ParentOrder("0005.HK", OrderSide.Sell, 200000, 68.00, OrderType.Limit);
		order.put(OrderField.STRATEGY.value(), "ICEBERG");
		order.put(OrderField.DISPLAY_QUANTITY.value(), new Double(20000));
		return order;
	}
	
	@Test
	public void test() {
		ParentOrder order = strategy.getParentOrder();
		// test display quantity
		timePass(10000);
		assertChildOrder(ASK, order.get(Double.TYPE, OrderField.DISPLAY_QUANTITY.value()), true);
		
		// change the best bid to 10000@68.25, check child order is peg to market
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, 10000, 68.25, "", "");
		timePass(10000);
		assertChildOrder(68.25, order.get(Double.TYPE, OrderField.DISPLAY_QUANTITY.value()), true);
		
		// test refilling of display quantity
		exchange.enterOrder("0005.HK", webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.BID, 12000, 68.25, "", "");
		timePass(10000);
		assertNoOfChildOrders(2);
		assertAtMarketQty(order.get(Double.TYPE, OrderField.DISPLAY_QUANTITY.value()));
		
		// check modification of display quantity
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put(OrderField.ID.value(), order.getId());
		fields.put(OrderField.DISPLAY_QUANTITY.value(), new Double(10000));
		AmendStrategyOrderEvent amendEvent = new AmendStrategyOrderEvent(order.getId(), null, "", "", fields);
		eventManager.sendEvent(amendEvent);
		timePass(10000);
		assertAtMarketQty(10000);
	}


}
