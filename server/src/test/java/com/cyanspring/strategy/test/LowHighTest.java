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
import java.util.Map.Entry;

import org.junit.Test;

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.strategy.MultiInstrumentStrategyTest;

public class LowHighTest extends MultiInstrumentStrategyTest {

	@Override
	protected void setupOrderBook() {
		// TODO Auto-generated method stub

	}

	@Override
	protected DataObject createData() {
		// LOW_HIGH
		Map<String, Object> lowHigh = new HashMap<String, Object>();
		lowHigh.put(OrderField.STRATEGY.value(), "LOW_HIGH");
		String strategyId = IdGenerator.getInstance().getNextID() + "S";
		lowHigh.put(OrderField.ID.value(), strategyId);
		lowHigh.put(OrderField.STATE.value(), StrategyState.Paused);
		MultiInstrumentStrategyData data = new MultiInstrumentStrategyData(lowHigh);
		
		Map<String, Object> instrLowHigh1 = new HashMap<String, Object>();
		instrLowHigh1.put(OrderField.SYMBOL.value(), "0001.HK");
		instrLowHigh1.put("Qty", 2000.0);
		instrLowHigh1.put("Low flat", 88.0);
		instrLowHigh1.put("Low take", 87.0);
		instrLowHigh1.put("Low stop", 82.0);
		Instrument instr1 = new Instrument("0001.HK");
		instr1.update(instrLowHigh1);
		data.getInstrumentData().put(instr1.getId(), instr1);
		
		Map<String, Object> instrLowHigh2 = new HashMap<String, Object>();
		instrLowHigh2.put(OrderField.SYMBOL.value(), "0005.HK");
		instrLowHigh2.put("Qty", 2000.0);
		instrLowHigh2.put("High stop", 69.0);
		instrLowHigh2.put("High take", 68.5);
		instrLowHigh2.put("High flat", 68.2);
		instrLowHigh2.put("Low flat", 68.2);
		instrLowHigh2.put("Low take", 67.9);
		instrLowHigh2.put("Low stop", 67.4);
		instrLowHigh2.put("Shortable", true);
		Instrument instr2 = new Instrument("0005.HK");
		instr2.update(instrLowHigh2);
		data.getInstrumentData().put(instr2.getId(), instr2);
		return data;
	}
	
	@Test
	public void testInitialReleases() {
		timePass(3000);
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low take");
			assertChildOrder(entry.getKey(), OrderSide.Buy, price, qty, false);
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				price = instr.get(double.class, "High take");
				assertChildOrder(entry.getKey(), OrderSide.Sell, price, qty, false);
			}
		}
	}
	
	@Test
	public void testLowFlatPlacement() {
		timePass(strategy.getLpInterval());
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low take");
			assertChildOrder(entry.getKey(), OrderSide.Buy, price, qty, false);
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				price = instr.get(double.class, "High take");
				assertChildOrder(entry.getKey(), OrderSide.Sell, price, qty, false);
			}
		}
		
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			String symbol = instr.getSymbol();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low take");
			// hit low take orders
			exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
					webcurve.common.BaseOrder.SIDE.ASK, (int)qty, price, "", "");
		}
		
		timePass(strategy.getLpInterval());

		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low flat");
			assertChildOrder(entry.getKey(), OrderSide.Sell, price, qty, true);
		}
	}
	
	@Test
	public void testLowStopPlacement() {
		timePass(strategy.getLpInterval());
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low take");
			assertChildOrder(entry.getKey(), OrderSide.Buy, price, qty, false);
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				price = instr.get(double.class, "High take");
				assertChildOrder(entry.getKey(), OrderSide.Sell, price, qty, false);
			}
		}
		
		// taking position
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			String symbol = instr.getSymbol();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low take");
			// hit low take orders
			exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
					webcurve.common.BaseOrder.SIDE.ASK, (int)qty, price, "", "");
		}
		
		timePass(strategy.getLpInterval());
		//assert position taken
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double position = instr.getPosition();
			assertTrue(PriceUtils.Equal(qty, position));
		}
		
		// produce low stop market conditions
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			String symbol = instr.getSymbol();
			double price = instr.get(double.class, "Low stop");
			exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
					webcurve.common.BaseOrder.SIDE.BID, 200000, price, "", "");
			// produce last price at low stop
			exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
					webcurve.common.BaseOrder.SIDE.ASK, 2000, price, "", "");
		}
		
		timePass(strategy.getLpInterval());
		// assertPosition flat
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double position = instr.getPosition();
			assertTrue(PriceUtils.Equal(0, position));
		}

	}
	
	@Test
	public void testHighFlatPlacement() {
		timePass(strategy.getLpInterval());
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low take");
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				price = instr.get(double.class, "High take");
				assertChildOrder(entry.getKey(), OrderSide.Sell, price, qty, false);
			}
		}
		
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				String symbol = instr.getSymbol();
				double qty = instr.get(double.class, OrderField.QUANTITY.value());
				double price = instr.get(double.class, "High take");
				// hit high take orders
				exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
						webcurve.common.BaseOrder.SIDE.BID, (int)qty, price, "", "");
			}
		}
		
		timePass(strategy.getLpInterval());

		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				double qty = instr.get(double.class, OrderField.QUANTITY.value());
				double price = instr.get(double.class, "High flat");
				assertChildOrder(entry.getKey(), OrderSide.Buy, price, qty, true);
			}
		}
	}
	
	@Test
	public void testHighStopPlacement() {
		timePass(strategy.getLpInterval());
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			double qty = instr.get(double.class, OrderField.QUANTITY.value());
			double price = instr.get(double.class, "Low take");
			assertChildOrder(entry.getKey(), OrderSide.Buy, price, qty, false);
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				price = instr.get(double.class, "High take");
				assertChildOrder(entry.getKey(), OrderSide.Sell, price, qty, false);
			}
		}
		
		// taking position
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				String symbol = instr.getSymbol();
				double qty = instr.get(double.class, OrderField.QUANTITY.value());
				double price = instr.get(double.class, "High take");
				// hit high take orders
				exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
						webcurve.common.BaseOrder.SIDE.BID, (int)qty, price, "", "");
			}
		}
		
		timePass(strategy.getLpInterval());
		//assert position taken
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				double qty = instr.get(double.class, OrderField.QUANTITY.value());
				double position = instr.getPosition();
				assertTrue(PriceUtils.Equal(qty, -position));
			}
		}
		
		// produce high stop market conditions
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				String symbol = instr.getSymbol();
				double price = instr.get(double.class, "High stop");
				exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
						webcurve.common.BaseOrder.SIDE.ASK, 200000, price, "", "");
				// produce last price at low stop
				exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
						webcurve.common.BaseOrder.SIDE.BID, 2000, price, "", "");
			}
		}
		
		timePass(strategy.getLpInterval());
		// assertPosition flat
		for(Entry<String, Instrument> entry: strategy.getData().getInstrumentData().entrySet()) {
			Instrument instr = entry.getValue();
			boolean shortable = instr.get(false, boolean.class, "Shortable");
			if(shortable) {
				double position = instr.getPosition();
				assertTrue(PriceUtils.Equal(0, position));
			}
		}

	}

}
