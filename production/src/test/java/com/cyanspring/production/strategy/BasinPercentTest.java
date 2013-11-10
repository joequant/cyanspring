package com.cyanspring.production.strategy;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.goldtree.strategy.Basin;
import com.cyanspring.server.strategy.MultiInstrumentStrategyTest;

public class BasinPercentTest extends MultiInstrumentStrategyTest {
	private static final String SYMBOL = "0005.HK";

	@Override
	protected DataObject createData() {
		Map<String, Object> strategyLevelParams = new HashMap<String, Object>();
		strategyLevelParams.put(OrderField.STRATEGY.value(), "BASIN");
		String strategyId = IdGenerator.getInstance().getNextID() + "S";
		strategyLevelParams.put(OrderField.ID.value(), strategyId);
		strategyLevelParams.put(OrderField.STATE.value(), StrategyState.Paused);
		MultiInstrumentStrategyData data = new MultiInstrumentStrategyData(strategyLevelParams);

		Instrument instr = new Instrument(SYMBOL);
		instr.put(OrderField.POSITION.value(), 2000.0);
		instr.put(OrderField.POS_AVGPX.value(), 68.3);
		instr.put(Basin.FIELD_MIN_WIN_PERCENT, 5.0);
		instr.put(Basin.FIELD_LOW_FALL_PERCENT, 10.0);
		data.getInstrumentData().put(instr.getId(), instr);
		
		return data;
	}

	@Override
	protected void setupOrderBook() {
		exchange.reset();
	}

	@Test
	public void testHighFall() {
		setQuote(SYMBOL, 68.1, 20000, 68.3, 20000, 68.3, 68.3, 68.1);
		timePass(strategy.getLpInterval());
		Instrument instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		
		setQuote(SYMBOL, 68.1, 20000, 68.3, 20000, 71.8, 71.7, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		
		setQuote(SYMBOL, 69.7, 20000, 69.8, 20000, 71.0, 71.8, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);
		
		// high fall
		setQuote(SYMBOL, 69.7, 20000, 69.8, 20000, 69.8, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		ChildOrder child = childOrders.get(0);
		assertTrue(child.getPrice() == 69.7);
		assertTrue(child.getSymbol() == SYMBOL);
		assertTrue(child.getSide() == OrderSide.Sell);
		assertTrue(child.getQuantity() == instr.getPosition());

		// missing bid
		setQuote(SYMBOL, 0, 0, 69.8, 20000, 69.6, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		child = childOrders.get(0);
		assertTrue(child.getPrice() == 69.6);
		assertTrue(child.getQuantity() == instr.getPosition());

		// bid volume is less than postion
		setQuote(SYMBOL, 69.7, 400, 69.8, 20000, 69.6, 71.3, 68.1);
		timePass(strategy.getLpInterval());
		instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		child = childOrders.get(0);
		assertTrue(PriceUtils.Equal(child.getPrice(), 69.65));
		assertTrue(child.getQuantity() == instr.getPosition());
	}
	
	@Test
	public void testLowFall() {
		Instrument instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		// low fall
		setQuote(SYMBOL, 65.3, 20000, 65.4, 20000, 61.5, 71.3, 65.3);
		timePass(strategy.getLpInterval());
		instr = strategy.getData().getInstrumentBySymbol(SYMBOL);
		List<ChildOrder> childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 0);

		setQuote(SYMBOL, 65.3, 20000, 65.4, 20000, 61.4, 71.3, 65.3);
		timePass(strategy.getLpInterval());
		childOrders = strategy.getOpenChildOrdersByParent(instr.getId());
		assertTrue(childOrders.size() == 1);
		ChildOrder child = childOrders.get(0);
		assertTrue(PriceUtils.Equal(child.getPrice(), 65.3));
		assertTrue(child.getQuantity() == instr.getPosition());
		
	}	
}
