package com.cyanspring.server.strategy;

import static org.junit.Assert.assertTrue;

import java.util.List;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleinstrument.SingleInstrumentStrategy;

public abstract class SingleInstrumentStrategyTest extends StrategyTest {
	
	protected SingleInstrumentStrategy strategy;
	protected String getSymbol() {
		return "0005.HK";
	}

	@Override
	protected void createStrategy() {
		super.createStrategy();
		assertTrue(_strategy instanceof SingleInstrumentStrategy);
		strategy = (SingleInstrumentStrategy)_strategy;
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
	
	// some constant values
	static protected final double BID = 68.10;
	static protected final int BID_VOL = 20000;
	static protected final double ASK = 68.30;
	static protected final int ASK_VOL = 40000;
	static protected final double LAST = 68.25; 
	static protected final int LAST_VOL = 2000; 
	
	
	@Override
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


}
