package com.cyanspring.server.strategy;

import static org.junit.Assert.assertTrue;

import java.util.List;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;

public abstract class  MultiInstrumentStrategyTest extends StrategyTest {

	protected MultiInstrumentStrategy strategy;

	@Override
	protected void createStrategy() {
		super.createStrategy();
		assertTrue(_strategy instanceof MultiInstrumentStrategy);
		strategy = (MultiInstrumentStrategy)_strategy;
	}

	// asserts
	protected void assertChildOrder(String id, OrderSide side, double price, double quantity, boolean only) {
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(id);
		if(only)
			 assertTrue(childOrders.size() == 1);
		 
		boolean found = false;
		for(ChildOrder order: childOrders) {
			if(PriceUtils.Equal(order.getPrice(), price) &&
			    PriceUtils.Equal(order.getQuantity(), quantity) &&
			    order.getSide().equals(side)) {
				 found = true;
			break;
			}
		}
		assertTrue(found);
	}

}
