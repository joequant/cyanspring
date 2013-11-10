package com.cyanspring.production.strategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyEvent;
import com.cyanspring.goldtree.strategy.Basin;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class BasinTemplate {

	static void saveXML(String name, AsyncEvent event, XStream xstream) throws IOException {
		File file = new File(name);
		file.createNewFile();
		FileOutputStream os = new FileOutputStream(file);
		xstream.toXML(event, os);
	}
	
	public static void main(String args[]) throws IOException {
		Map<String, Object> strategyLevel = new HashMap<String, Object>();
		strategyLevel.put(OrderField.STRATEGY.value(), "BASIN");
		
		List<Map<String, Object>> instruments = new ArrayList<Map<String, Object>>();
		Map<String, Object> instr = new HashMap<String, Object>();
		instr.put(OrderField.SYMBOL.value(), "5");
		instr.put(OrderField.POSITION.value(), 2000.0);
		instr.put(OrderField.POS_AVGPX.value(), 68.3);
		instr.put(Basin.FIELD_MIN_WIN_PERCENT, 5.0);
		instr.put(Basin.FIELD_LOW_FALL_PERCENT, 10.0);
		instruments.add(instr);
		
		NewMultiInstrumentStrategyEvent event 
			= new NewMultiInstrumentStrategyEvent(null, null, strategyLevel, instruments);
		
		XStream xstream = new XStream(new DomDriver());
		saveXML("templates/BASIN.xml", event, xstream);
	}
}
