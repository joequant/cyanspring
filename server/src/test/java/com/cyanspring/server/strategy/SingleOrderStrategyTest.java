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

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public abstract class SingleOrderStrategyTest extends StrategyTest implements ApplicationContextAware {

	protected SingleOrderStrategy strategy;
	protected String getSymbol() {
		return "0005.HK";
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	
	// some constant values
	static protected final double BID = 68.10;
	static protected final int BID_VOL = 20000;
	static protected final double ASK = 68.30;
	static protected final int ASK_VOL = 40000;
	static protected final double LAST = 68.25; 
	static protected final int LAST_VOL = 2000; 
	
	protected void setupOrderBook() {
		exchange.reset();
		//setting up depth to 
		// bid 20000@68.20 
		// ask 40000@68.30
		// las 20000@68.25
		exchange.enterOrder(getSymbol(), webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.BID, LAST_VOL, LAST, "", "");
		exchange.enterOrder(getSymbol(), webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, LAST_VOL, LAST, "", "");
		exchange.enterOrder(getSymbol(), webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.BID, BID_VOL, BID, "", "");
		exchange.enterOrder(getSymbol(), webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, ASK_VOL, ASK, "", "");
	
	}

	protected abstract ParentOrder createData();

	@Override
	protected void createStrategy() {
		super.createStrategy();
		assertTrue(_strategy instanceof SingleOrderStrategy);
		strategy = (SingleOrderStrategy)_strategy;
	}
	
	// utils
	protected double getBest() {
		return strategy.getParentOrder().getSide().isBuy()?BID:ASK;
	}

	// asserts
	protected void assertChildOrder(double price, double quantity, boolean only) {
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(strategy.getId());
		if(only)
			 assertTrue(childOrders.size() == 1);
		 
		boolean found = false;
		for(ChildOrder order: childOrders) {
			if(PriceUtils.Equal(order.getPrice(), price) &&
			    PriceUtils.Equal(order.getQuantity(), quantity)) {
				 found = true;
			break;
			}
		}
		assertTrue(found);
	}
	
	protected void assertAtMarketQty(double qty) {
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(strategy.getId());
		double total = 0;
		for(ChildOrder order: childOrders) {
			total += order.getQuantity() - order.getCumQty();
		}
		assertTrue(PriceUtils.Equal(total, qty));
	}
	
	protected void assertNoOfChildOrders(int count) {
		assertTrue(count == strategy.getOpenChildOrdersByParent(strategy.getId()).size());
	}
	
}
