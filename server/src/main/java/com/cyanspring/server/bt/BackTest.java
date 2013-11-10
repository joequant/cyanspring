package com.cyanspring.server.bt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.test.TestScheduleManager;
import com.cyanspring.common.marketdata.TickDataException;
import com.cyanspring.common.strategy.GlobalStrategySettings;
import com.cyanspring.server.BusinessManager;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class BackTest implements IAsyncEventListener {
	
	private static final Logger log = LoggerFactory
			.getLogger(BackTest.class);
	
	@Autowired
	private IAsyncEventManager eventManager;
	
	@Autowired
	private BusinessManager businessManager;

	@Autowired
	private TestScheduleManager scheduleManager;
	
	@Autowired
	private ExchangeBT exchange;
	
	@Autowired
	GlobalStrategySettings globalStrategySettings;
	
	private String fileName;
	private XStream xstream = new XStream(new DomDriver());
	private Map<String, ParentOrder> parentOrders = new ConcurrentHashMap<String, ParentOrder>();
	private Map<String, MultiInstrumentStrategyData> multiData = new ConcurrentHashMap<String, MultiInstrumentStrategyData>();
	
	public void init() throws Exception {
		Clock.getInstance().setMode(Clock.Mode.MANUAL);
		scheduleManager.init();
		Clock.getInstance().addClockListener(scheduleManager);
		exchange.init();
		businessManager.init();
		
		eventManager.subscribe(UpdateParentOrderEvent.class, this);
		eventManager.subscribe(MultiInstrumentStrategyUpdateEvent.class, this);
		log.info("GlobalStrategySettings - checkAdjQuote: " + globalStrategySettings.isCheckAdjQuote());
	}
	
	public void start(String[] tickFiles) throws TickDataException, IOException {
		exchange.loadTickDataFiles(tickFiles);
		exchange.replay();
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void saveStrategies() throws IOException {
		List list = new ArrayList();
		for(ParentOrder order: parentOrders.values()) {
			list.add(new EnterParentOrderEvent(null, null, order.getFields(), "", false));
		}
		
		for(MultiInstrumentStrategyData data: multiData.values()) {
			List<Map<String, Object>> instrs = new ArrayList<Map<String, Object>>();
			for(Instrument instr: data.getInstrumentData().values()) {
				instrs.add(instr.getFields());
			}
			list.add(new NewMultiInstrumentStrategyEvent(null, null, data.getFields(), instrs));
		}
		String outputFileName = fileName;
		if(fileName.endsWith(".xml"))
			outputFileName = "backtest/out/" + fileName.substring(0, fileName.length()-".xml".length()) + ".out.xml";
		
		File dir = new File("backtest");
		if(!dir.exists())
			dir.mkdir();
		
		dir = new File("backtest/out");
		if(!dir.exists())
			dir.mkdir();
		
		File file = new File(outputFileName); 
		file.createNewFile();
		FileOutputStream os = new FileOutputStream(file);

		xstream.toXML(list, os);
		log.info("Strategy back test result is saved as: " + outputFileName);
	}
	public void stop() throws IOException {
		saveStrategies();
		System.exit(0);
	}
	
	@SuppressWarnings("unchecked")
	public void loadStrategyFile(String fileName) throws Exception {
		File file = new File(fileName);
		if(!file.exists())
			throw new Exception("File not found: " + fileName);
		
		RemoteAsyncEvent event = null;
		List<RemoteAsyncEvent> events = null;
		Object obj = xstream.fromXML(file);
		if(obj instanceof RemoteAsyncEvent) {
			event = (RemoteAsyncEvent)obj;
		} else if(obj instanceof List){
			events = (List<RemoteAsyncEvent>)obj;
			for(RemoteAsyncEvent e: events) {
				if(!(e instanceof RemoteAsyncEvent))
					throw new Exception("List contains object not a subclass of RemoteAsyncEvent");
			}
		} else
			throw new Exception("Object is not subclass of or a list of RemoteAsyncEvent");
		
		if(null == events)
			events = new ArrayList<RemoteAsyncEvent>();
		if(null != event)
			events.add(event);
		
		for(RemoteAsyncEvent remoteEvent: events) {
			eventManager.sendEvent(remoteEvent);
		}
		this.fileName = file.getName();
	}
	
	private void processUpdateParentOrderEvent(UpdateParentOrderEvent event){
		parentOrders.put(event.getParent().getId(), event.getParent());
	}
	
	private void processMultiInstrumentStrategyUpdateEvent(MultiInstrumentStrategyUpdateEvent event) {
		multiData.put(event.getStrategyData().getId(), event.getStrategyData());
	}
	
	@Override
	public void onEvent(AsyncEvent event) {
		if(event instanceof UpdateParentOrderEvent) {
			processUpdateParentOrderEvent((UpdateParentOrderEvent) event);
		} else if (event instanceof MultiInstrumentStrategyUpdateEvent) {
			processMultiInstrumentStrategyUpdateEvent((MultiInstrumentStrategyUpdateEvent) event);
		} else {
			log.error("Event not handled: " + event.getClass());
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		if(args.length < 2) {
			System.out.println("Usage: strategy_file tick_file1 tick_file2...");
			return;
		}
		DOMConfigurator.configure("conf/log4j_bt.xml");
		ApplicationContext context = new FileSystemXmlApplicationContext("conf/back_test.xml");
		
		// start server
		BackTest backTest = (BackTest)context.getBean("backTest");
		backTest.init();
		
		backTest.loadStrategyFile(args[0]);
		
		String[] tickFiles = new String[args.length-1];
		for(int i=0; i<tickFiles.length; i++) {
			tickFiles[i] = args[i+1];
		}
		
		backTest.start(tickFiles);
//		Thread.sleep(1000);
		backTest.stop();
	}


}
