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
package com.cyanspring.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyEvent;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class OrderGenerator implements IPlugin, IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(OrderGenerator.class);
	@Autowired
	IAsyncEventManager eventManager;

	public void init() {
		log.info("Initialising OrderGenerator");
		eventManager.subscribe(ServerReadyEvent.class, this);
	}
	
	@Override
	public void uninit() {
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof ServerReadyEvent) {
			ServerReadyEvent e = (ServerReadyEvent)event;
			if(!e.isReady())
				return;
			
			//createDollarNeutral();
			//eventManager.sendEvent(createPOV());
			//eventManager.sendEvent(createLowHigh());
			//eventManager.sendEvent(createStopWinLoss()) ;
		}
	}
	
	static EnterParentOrderEvent createSDMA() {
		// SDMA 
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
		fields.put(OrderField.SYMBOL.value(), "0005.HK");
		fields.put(OrderField.SIDE.value(), OrderSide.Buy.toString());
		fields.put(OrderField.TYPE.value(), OrderType.Limit.toString());
		fields.put(OrderField.PRICE.value(), "68.25");
		fields.put(OrderField.QUANTITY.value(), "2000");
		fields.put(OrderField.STRATEGY.value(), "SDMA");
		enterOrderEvent = new EnterParentOrderEvent(null, null, fields, IdGenerator.getInstance().getNextID(), false);
		return enterOrderEvent;
	}
	
	static EnterParentOrderEvent createPOV() {
		// POV
		HashMap<String, Object> fields;
		EnterParentOrderEvent enterOrderEvent;
		fields = new HashMap<String, Object>();
		fields.put(OrderField.SYMBOL.value(), "0005.HK");
		fields.put(OrderField.SIDE.value(), OrderSide.Buy.toString());
		fields.put(OrderField.TYPE.value(), OrderType.Limit.toString());
		fields.put(OrderField.PRICE.value(), "70.4");
		fields.put(OrderField.QUANTITY.value(), "80000");
		fields.put(OrderField.POV.value(), "30");
		fields.put(OrderField.POV_LIMIT.value(), "30");
		fields.put(OrderField.STRATEGY.value(), "POV");
		enterOrderEvent = new EnterParentOrderEvent(null, null, fields, IdGenerator.getInstance().getNextID(), false);
		return enterOrderEvent;
	}
	
	static NewMultiInstrumentStrategyEvent createDollarNeutral() {
		// DOLLAR_NEUTRAL
		Map<String, Object> strategyLevelParams = new HashMap<String, Object>();
		strategyLevelParams.put(OrderField.STRATEGY.value(), "DOLLAR_NEUTRAL");
		strategyLevelParams.put("Value", "50000");
		strategyLevelParams.put("Allow diff", "100");
		strategyLevelParams.put("High stop", "0.05");
		strategyLevelParams.put("High take", "0.02");
		strategyLevelParams.put("High flat", "0.01");
		strategyLevelParams.put("Low flat", "-0.01");
		strategyLevelParams.put("Low take", "-0.02");
		strategyLevelParams.put("Low stop", "-0.05");
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> instr1 = new HashMap<String, Object>();
		instr1.put(OrderField.SYMBOL.value(), "RIO.AX");
		instr1.put("Leg", "1");
		instr1.put("Weight", "1");
		instr1.put("Ref price", "55");
		list.add(instr1);
		
		Map<String, Object> instr2 = new HashMap<String, Object>();
		instr2.put(OrderField.SYMBOL.value(), "WBC.AX");
		instr2.put("Leg", "1");
		instr2.put("Weight", "2");
		instr2.put("Ref price", "20.5");
		list.add(instr2);
		
		Map<String, Object> instr3 = new HashMap<String, Object>();
		instr3.put(OrderField.SYMBOL.value(), "BHP.AX");
		instr3.put("Leg", "2");
		instr3.put("Weight", "1");
		instr3.put("Ref price", "37");
		list.add(instr3);

		Map<String, Object> instr4 = new HashMap<String, Object>();
		instr4.put(OrderField.SYMBOL.value(), "ANZ.AX");
		instr4.put("Leg", "2");
		instr4.put("Weight", "2");
		instr4.put("Ref price", "21.6");
		list.add(instr4);
		
		NewMultiInstrumentStrategyEvent newMultiInstrumentStrategyEvent 
			= new NewMultiInstrumentStrategyEvent(null, null, strategyLevelParams, list);
		
		return newMultiInstrumentStrategyEvent;
	}
	
	static NewMultiInstrumentStrategyEvent createLowHigh() {
		// LOW_HIGH
		Map<String, Object> lowHigh = new HashMap<String, Object>();
		lowHigh.put(OrderField.STRATEGY.value(), "LOW_HIGH");
		
		List<Map<String, Object>> listLowHigh = new ArrayList<Map<String, Object>>();
		Map<String, Object> instrLowHigh1 = new HashMap<String, Object>();
		instrLowHigh1.put(OrderField.SYMBOL.value(), "0001.HK");
		instrLowHigh1.put("Qty", "2000");
		instrLowHigh1.put("Low flat", "88");
		instrLowHigh1.put("Low take", "87");
		instrLowHigh1.put("Low stop", "82");
		listLowHigh.add(instrLowHigh1);
		
		Map<String, Object> instrLowHigh2 = new HashMap<String, Object>();
		instrLowHigh2.put(OrderField.SYMBOL.value(), "0005.HK");
		instrLowHigh2.put("Qty", "2000");
		instrLowHigh2.put("High stop", "69");
		instrLowHigh2.put("High take", "68.5");
		instrLowHigh2.put("High flat", "68.2");
		instrLowHigh2.put("Low flat", "68.2");
		instrLowHigh2.put("Low take", "67.9");
		instrLowHigh2.put("Low stop", "67.4");
		instrLowHigh2.put("Shortable", "true");
		listLowHigh.add(instrLowHigh2);
		
		NewMultiInstrumentStrategyEvent newLowHighStrategyEvent 
			= new NewMultiInstrumentStrategyEvent(null, null, lowHigh, listLowHigh);
		
		return newLowHighStrategyEvent;
	}
	
	static NewSingleInstrumentStrategyEvent createStopWinLoss() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(OrderField.SYMBOL.value(), "0005.HK");
		map.put(OrderField.STRATEGY.value(), "STOP_WIN_LOSS");
		map.put(OrderField.POSITION.value(), 2000.0);
		map.put(OrderField.POS_AVGPX.value(), 68.3);
		map.put("Min win", 3.0);
		map.put("High fall", 1.0);
		map.put("Low fall", 3.0);
		NewSingleInstrumentStrategyEvent event = new NewSingleInstrumentStrategyEvent(null, null, "", map);
		
		return event;
	}
	
	static void saveXML(String name, AsyncEvent event, XStream xstream) throws IOException {
		File file = new File(name);
		file.createNewFile();
		FileOutputStream os = new FileOutputStream(file);
		xstream.toXML(event, os);
	}
	
	public static void main(String[] args) throws IOException {
		XStream xstream = new XStream(new DomDriver());
		OrderGenerator.saveXML("templates/SDMA.xml", OrderGenerator.createSDMA(), xstream);
		OrderGenerator.saveXML("templates/POV.xml", OrderGenerator.createPOV(), xstream);
		OrderGenerator.saveXML("templates/LOW_HIGH.xml", OrderGenerator.createLowHigh(), xstream);
		OrderGenerator.saveXML("templates/DOLLAR_NEUTRAL.xml", OrderGenerator.createDollarNeutral(), xstream);
		
	}

}
