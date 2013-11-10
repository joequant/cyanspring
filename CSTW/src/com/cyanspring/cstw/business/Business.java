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
package com.cyanspring.cstw.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.BeanHolder;
import com.cyanspring.common.Clock;
import com.cyanspring.common.SystemInfo;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.data.AlertType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.event.order.InitClientEvent;
import com.cyanspring.common.event.order.InitClientRequestEvent;
import com.cyanspring.common.event.order.StrategySnapshotRequestEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.SingleOrderStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.system.NodeInfoEvent;
import com.cyanspring.common.event.system.ServerHeartBeatEvent;
import com.cyanspring.common.marketsession.DefaultStartEndTime;
import com.cyanspring.common.server.event.ServerReadyEvent;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.TimeUtil;
import com.cyanspring.cstw.event.ServerStatusEvent;
import com.cyanspring.cstw.gui.ServerStatusDisplay;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class Business {
	static Logger log = LoggerFactory.getLogger(Business.class);
	private XStream xstream = new XStream(new DomDriver());
	static private Business instance; // Singleton
	private String configPath;
	private SystemInfo systemInfo;
	private IRemoteEventManager eventManager;
	private OrderCachingManager orderManager;
	private String inbox;
	private String channel;
	private String nodeInfoChannel;
	private HashMap<String, Boolean> servers = new HashMap<String, Boolean>();
	private EventListener listener = new EventListener();
	private List<String> singleOrderDisplayFields;
	private List<String> singleInstrumentDisplayFields;
	private Map<String, Map<String, FieldDef>> singleOrderFieldDefs;
	private Map<String, Map<String, FieldDef>> singleInstrumentFieldDefs;
	private List<String> multiInstrumentDisplayFields;
	private Map<String, MultiInstrumentStrategyDisplayConfig> multiInstrumentFieldDefs;
	private ScheduleManager scheduleManager = new ScheduleManager();
	private AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private int heartBeatInterval = 5000; // 5 seconds
	private HashMap<String, Date> lastHeartBeats = new HashMap<String, Date>();
	private DefaultStartEndTime defaultStartEndTime;
	private Map<AlertType, Integer> alertColorConfig;
	
	// singleton implementation
	private Business() {
	}
	
	static public Business getInstance() {
		if (null == instance) {
			instance = new Business();
		}
		return instance;
	}


	class EventListener implements IAsyncEventListener 
	{
		@Override
		public void onEvent(AsyncEvent event) {
//			log.info("Received message: " + message);
			if (event instanceof NodeInfoEvent ){
				NodeInfoEvent nodeInfo = (NodeInfoEvent)event;
				if(nodeInfo.getServer()) {
					Boolean serverIsUp = servers.get(nodeInfo.getInbox());
					if(serverIsUp != null && serverIsUp) {
						log.error("ignore since server " + nodeInfo.getInbox() + " is still up");
						return;
					}
					servers.put(nodeInfo.getInbox(), true);
					lastHeartBeats.put(nodeInfo.getInbox(), Clock.getInstance().now());	
				}
			} else if (event instanceof InitClientEvent) {
				log.debug("Received event: " + event);
				InitClientEvent initClientEvent = (InitClientEvent)event;
				singleOrderFieldDefs = initClientEvent.getSingleOrderFieldDefs();
				singleOrderDisplayFields = initClientEvent.getSingleOrderDisplayFields();
				singleInstrumentFieldDefs = initClientEvent.getSingleInstrumentFieldDefs();
				singleInstrumentDisplayFields = initClientEvent.getSingleInstrumentDisplayFields();
				defaultStartEndTime = initClientEvent.getDefaultStartEndTime();
				multiInstrumentDisplayFields = initClientEvent.getMultiInstrumentDisplayFields();
				multiInstrumentFieldDefs = initClientEvent.getMultiInstrumentStrategyFieldDefs();
				try {
					eventManager.sendRemoteEvent(new StrategySnapshotRequestEvent(null, initClientEvent.getSender()));
					eventManager.sendEvent(new ServerStatusEvent(initClientEvent.getSender(), true));	
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}

			} else if (event instanceof ServerReadyEvent) {
				InitClientRequestEvent request = new InitClientRequestEvent(null, ((ServerReadyEvent) event).getSender());
				try {
					eventManager.sendRemoteEvent(request);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
			} else if (event instanceof ServerHeartBeatEvent) {
				processServerHeartBeatEvent((ServerHeartBeatEvent)event);
			} else if (event instanceof SingleOrderStrategyFieldDefUpdateEvent) {
				processingSingleOrderStrategyFieldDefUpdateEvent((SingleOrderStrategyFieldDefUpdateEvent)event);
			} else if (event instanceof SingleInstrumentStrategyFieldDefUpdateEvent) {
				processingSingleInstrumentStrategyFieldDefUpdateEvent((SingleInstrumentStrategyFieldDefUpdateEvent)event);
			} else if (event instanceof MultiInstrumentStrategyFieldDefUpdateEvent) {
				processingMultiInstrumentStrategyFieldDefUpdateEvent((MultiInstrumentStrategyFieldDefUpdateEvent)event);
			} else if (event instanceof AsyncTimerEvent) {
				processAsyncTimerEvent((AsyncTimerEvent)event);
			} else {
				log.error("I dont expect this event: " + event);
			}
		}
	}
	
	synchronized private void processingSingleOrderStrategyFieldDefUpdateEvent(SingleOrderStrategyFieldDefUpdateEvent event) {
		singleOrderFieldDefs.put(event.getName(), event.getFieldDefs());
		log.info("Single-order strategy field def update: " + event.getName());		
	}
	
	public void processingSingleInstrumentStrategyFieldDefUpdateEvent(
			SingleInstrumentStrategyFieldDefUpdateEvent event) {
		singleInstrumentFieldDefs.put(event.getName(), event.getFieldDefs());
		log.info("Single-instrument strategy field def update: " + event.getName());		
	}

	synchronized private void processingMultiInstrumentStrategyFieldDefUpdateEvent(MultiInstrumentStrategyFieldDefUpdateEvent event) {
		multiInstrumentFieldDefs.put(event.getConfig().getStrategy(), event.getConfig());
		log.info("Multi-Instrument strategy field def update: " + event.getConfig().getStrategy());
	}
	
	private void loadSystemInfo() throws IOException {
	    String strFile = configPath + SystemInfo.class.getSimpleName() + ".xml";
		File file = new File(strFile);
		if (!file.exists()) {
			log.info("writing default SystemInfo file: " + strFile);
			file.createNewFile();
			systemInfo = new SystemInfo();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(systemInfo, os);
			os.close();
			log.info("writen default SystemInfo file: " + strFile);
		} else {
			log.info("loading SystemInfo file: " + strFile);
			systemInfo = (SystemInfo)xstream.fromXML(file);
			log.info("loaded SystemInfo file: " + strFile);
		}
		log.info("SystemInfo: " + systemInfo);
	}
	
	public void processServerHeartBeatEvent(ServerHeartBeatEvent event) {
		lastHeartBeats.put(event.getSender(), Clock.getInstance().now());	
	}

	public void processAsyncTimerEvent(AsyncTimerEvent event) {
		for(Entry<String, Date> entry: lastHeartBeats.entrySet()) {
			if(TimeUtil.getTimePass(entry.getValue()) > heartBeatInterval) {
				log.debug("Sending server down event: " + entry.getKey());
				servers.put(entry.getKey(), false);
				eventManager.sendEvent(new ServerStatusEvent(entry.getKey(), false));	
			} else { // server heart beat can go back up
				Boolean up = servers.get(entry.getKey());
				if(null != up && !up) {
					log.debug("Sending server up event: " + entry.getKey());
					servers.put(entry.getKey(), true);
					eventManager.sendEvent(new ServerStatusEvent(entry.getKey(), true));	
				}
			}
		}
	}

	public void init() throws Exception {
		log.info("Initializing business obj...");
		loadSystemInfo();
		
		// create node.info subscriber and publisher
		this.channel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "channel"; 
		this.nodeInfoChannel = systemInfo.getEnv() + "." + systemInfo.getCategory() + "." + "node";
		InetAddress addr = InetAddress.getLocalHost();
		String hostName = addr.getHostName();
		String userName = System.getProperty("user.name");
		userName = userName == null? "" : userName;
		this.inbox = hostName + "." + userName + "." + IdGenerator.getInstance().getNextID();
		BeanHolder beanHolder = BeanHolder.getInstance();
		if(beanHolder == null)
			throw new Exception("BeanHolder is not yet initialised");
		//ActiveMQObjectService transport = new ActiveMQObjectService();
		//transport.setUrl(systemInfo.getUrl());

		//eventManager = new RemoteEventManager(beanHolder.getTransportService());
		eventManager = beanHolder.getEventManager();
		alertColorConfig = beanHolder.getAlertColorConfig();
		
		boolean ok = false;
		while(!ok) {
			try {
				eventManager.init(channel, inbox);
			} catch (Exception e) {
				log.error(e.getMessage());
				log.debug("Retrying in 3 seconds...");
				ok = false;
				Thread.sleep(3000);
				continue;
			}
			ok = true;
		} 
		
		eventManager.addEventChannel(this.channel);
		eventManager.addEventChannel(this.nodeInfoChannel);

		orderManager = new OrderCachingManager(eventManager);
		orderManager.init();

		ServerStatusDisplay.getInstance().init();
		
		eventManager.subscribe(NodeInfoEvent.class, listener);
		eventManager.subscribe(InitClientEvent.class, listener);		
		eventManager.subscribe(ServerHeartBeatEvent.class, listener);		
		eventManager.subscribe(ServerReadyEvent.class, listener);		
		eventManager.subscribe(SingleOrderStrategyFieldDefUpdateEvent.class, listener);		
		eventManager.subscribe(MultiInstrumentStrategyFieldDefUpdateEvent.class, listener);		
		
		//schedule timer
		scheduleManager.scheduleRepeatTimerEvent(heartBeatInterval , listener, timerEvent);

	}
	
	public void start() throws Exception {
		// publish my node info
		NodeInfoEvent nodeInfo = new NodeInfoEvent(null, null, false, true, inbox, inbox);
		eventManager.publishRemoteEvent(nodeInfoChannel, nodeInfo);
		log.info("Published my node info");
		
	}
	public int getHeartBeatInterval() {
		return heartBeatInterval;
	}

	public void setHeartBeatInterval(int heartBeatInterval) {
		this.heartBeatInterval = heartBeatInterval;
	}

	public void stop() {
		log.info("Stopping business object...");
		try {
			eventManager.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public SystemInfo getSystemInfo() {
		return systemInfo;
	}

	public IRemoteEventManager getEventManager() {
		return eventManager;
	}

	public OrderCachingManager getOrderManager() {
		return orderManager;
	}

	public XStream getXstream() {
		return xstream;
	}

	public ScheduleManager getScheduleManager() {
		return scheduleManager;
	}

	public String getInbox() {
		return inbox;
	}

	synchronized public List<String> getParentOrderDisplayFields() {
		return singleOrderDisplayFields;
	}

	synchronized public DefaultStartEndTime getDefaultStartEndTime() {
		return defaultStartEndTime;
	}

	synchronized public List<String> getSingleOrderDisplayFields() {
		return singleOrderDisplayFields;
	}

	synchronized public Map<String, Map<String, FieldDef>> getSingleOrderFieldDefs() {
		return singleOrderFieldDefs;
	}

	synchronized public List<String> getSingleInstrumentDisplayFields() {
		return singleInstrumentDisplayFields;
	}
	synchronized public Map<String, Map<String, FieldDef>> getSingleInstrumentFieldDefs() {
		return singleInstrumentFieldDefs;
	}

	synchronized public Map<String, MultiInstrumentStrategyDisplayConfig> getMultiInstrumentFieldDefs() {
		return multiInstrumentFieldDefs;
	}

	synchronized public List<String> getMultiInstrumentDisplayFields() {
		return multiInstrumentDisplayFields;
	}

	synchronized public List<String> getSingleOrderAmendableFields(String key) {
		List<String> result = new ArrayList<String>();
		Map<String, FieldDef> fieldDefs = singleOrderFieldDefs.get(key);
		if(null != fieldDefs) {
			for(FieldDef fieldDef: fieldDefs.values()) {
				if(fieldDef.isAmendable())
					result.add(fieldDef.getName());
			}
		}
		return result;
	}

	synchronized public List<String> getSingleInstrumentAmendableFields(String key) {
		List<String> result = new ArrayList<String>();
		Map<String, FieldDef> fieldDefs = singleInstrumentFieldDefs.get(key);
		if(null != fieldDefs) {
			for(FieldDef fieldDef: fieldDefs.values()) {
				if(fieldDef.isAmendable())
					result.add(fieldDef.getName());
			}
		}
		return result;
	}

	public Map<AlertType, Integer> getAlertColorConfig() {
		return alertColorConfig;
	}
	
	
}
