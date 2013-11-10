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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.sample.multiinstrument.dollarneutral.DollarNeutralStrategy;
import com.cyanspring.server.strategy.MultiInstrumentStrategyTest;

public class DollarNeutralTest extends MultiInstrumentStrategyTest {

	@Override
	protected DataObject createData() {
		// DOLLAR_NEUTRAL
		Map<String, Object> strategyLevelParams = new HashMap<String, Object>();
		strategyLevelParams.put(OrderField.STRATEGY.value(), "DOLLAR_NEUTRAL");
		String strategyId = IdGenerator.getInstance().getNextID() + "S";
		strategyLevelParams.put(OrderField.ID.value(), strategyId);
		strategyLevelParams.put(OrderField.STATE.value(), StrategyState.Paused);
		MultiInstrumentStrategyData data = new MultiInstrumentStrategyData(strategyLevelParams);

		data.put(OrderField.STRATEGY.value(), "DOLLAR_NEUTRAL");
		data.put("Value", 50000.0);
		data.put("Allow diff", 100.0);
		data.put("High stop", 0.05);
		data.put("High take", 0.02);
		data.put("High flat", 0.01);
		data.put("Low flat", -0.01);
		data.put("Low take", -0.02);
		data.put("Low stop", -0.05);
		
		Instrument instr1 = new Instrument("RIO.AX");
		instr1.put("Leg", 1);
		instr1.put("Weight", 1.0);
		instr1.put("Ref price", 55.0);
		data.getInstrumentData().put(instr1.getId(), instr1);
		
		Instrument instr2 = new Instrument("WBC.AX");
		instr2.put("Leg", 1);
		instr2.put("Weight", 2.0);
		instr2.put("Ref price", 20.5);
		data.getInstrumentData().put(instr2.getId(), instr2);
		
		Instrument instr3 = new Instrument("BHP.AX");
		instr3.put("Leg", 2);
		instr3.put("Weight", 1.0);
		instr3.put("Ref price", 37.0);
		data.getInstrumentData().put(instr3.getId(), instr3);

		Instrument instr4 = new Instrument("ANZ.AX");
		instr4.put("Leg", 2);
		instr4.put("Weight", 2.0);
		instr4.put("Ref price", 21.6);
		data.getInstrumentData().put(instr4.getId(), instr4);
		
		return data;
	}

	@Override
	protected void setupOrderBook() {
		exchange.reset();

	}
	
	@Test
	public void testNeutral() {
		double value = strategy.getData().get(double.class, "Value");
		double diff = strategy.getData().get(double.class, "Allow diff");
		double leg1Value = 0;
		double leg2Value = 0;
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			double price = instr.get(double.class, "Ref price");
			double qty = instr.get(double.class, "Ref qty");
			int leg = instr.get(int.class, "Leg");
			if(leg == 1)
				leg1Value += price * qty;
			else
				leg2Value += price * qty;
		}
		assertTrue(Math.abs(value - leg1Value) <= diff);
		assertTrue(Math.abs(value - leg2Value) <= diff);
		
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			String symbol = instr.getSymbol();
			double price = instr.get(double.class, "Ref price");
			ITickTable tickTable = tickTableManager.getTickTable(symbol);
			enterExchangeBuyOrder(symbol, tickTable.tickDown(price, false), 200000);
			enterExchangeSellOrder(symbol, tickTable.tickUp(price, true), 200000);
		}
		
		timePass(strategy.getLpInterval());
		assertTrue(strategy.isZeroPosition());
	}
	
	
	private void setupMarket(int upLeg, int ticks) {
		exchange.reset();
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			String symbol = instr.getSymbol();
			double price = instr.get(double.class, "Ref price");
			ITickTable tickTable = tickTableManager.getTickTable(symbol);
			int leg = instr.get(int.class, "Leg");
			if(leg == upLeg) {
				double mp = tickTable.tickUp(price, ticks, false);
				enterExchangeBuyOrder(symbol, mp, 200000);
				enterExchangeSellOrder(symbol, mp, 2000);
				enterExchangeSellOrder(symbol, tickTable.tickUp(mp, true), 200000);
			} else {
				double mp = tickTable.tickDown(price, ticks, false);
				enterExchangeBuyOrder(symbol, mp, 200000);
				enterExchangeSellOrder(symbol, mp, 2000);
				enterExchangeSellOrder(symbol, tickTable.tickUp(mp, true), 200000);
			}
			exchange.getBook(symbol).show();
		}
	}
	
	@Test
	public void testHighTake() {
		setupMarket(1, 6);
		timePass(strategy.getLpInterval());
		assertTrue(!strategy.isZeroPosition());
	}
	
	@Test
	public void testHighFlat() {
		testHighTake();
		setupMarket(1, 1);
		timePass(strategy.getLpInterval());
		assertTrue(strategy.isZeroPosition());
		// we should have made a profit!
		assertTrue(strategy.getPnL() > 0);
	}
	
	@Test
	public void testHighStop() {
		testHighTake();
		setupMarket(1, 14);
		timePass(strategy.getLpInterval());
		assertTrue(strategy.isZeroPosition());
		// we have cut loss, we should have made a negative P&L
		assertTrue(strategy.getPnL() < 0);
	}
	
	@Test
	public void testLowTake() {
		setupMarket(2, 6);
		timePass(strategy.getLpInterval());
		assertTrue(!strategy.isZeroPosition());
	}
	
	@Test
	public void testLowFlat() {
		testLowTake();
		setupMarket(2, 1);
		timePass(strategy.getLpInterval());
		assertTrue(strategy.isZeroPosition());
		// we should have made a profit!
		assertTrue(strategy.getPnL() > 0);
	}
	
	@Test
	public void testLowStop() {
		testLowTake();
		setupMarket(2, 14);
		timePass(strategy.getLpInterval());
		assertTrue(strategy.isZeroPosition());
		// we have cut loss, we should have made a negative P&L
		assertTrue(strategy.getPnL() < 0);
	}
	
	@Test
	public void testLegged() {
		testLowTake();
		for(Instrument instr: strategy.getData().getInstrumentData().values()) {
			// remove one leg
			instr.put(OrderField.POSITION.value(), 0.0);
			break;
		}
		assertTrue(strategy instanceof DollarNeutralStrategy);
		assertTrue(((DollarNeutralStrategy)strategy).isLegged());
		
		timePass(strategy.getLpInterval());
		// we should have recovered the leg
		assertTrue(!((DollarNeutralStrategy)strategy).isLegged());
	}

}
