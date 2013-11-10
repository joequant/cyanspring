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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.business.OrderException;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.DownStreamManager;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.AmendStrategyOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelStrategyOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.InitClientEvent;
import com.cyanspring.common.event.order.InitClientRequestEvent;
import com.cyanspring.common.event.order.UpdateParentOrderEvent;
import com.cyanspring.common.event.strategy.AddStrategyEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.NewMultiInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.NewSingleInstrumentStrategyReplyEvent;
import com.cyanspring.common.marketsession.DefaultStartEndTime;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.staticdata.TickTableManager;
import com.cyanspring.common.strategy.GlobalStrategySettings;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.strategy.IStrategyContainer;
import com.cyanspring.common.strategy.IStrategyFactory;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.validation.OrderValidationException;
import com.cyanspring.core.event.AsyncEventProcessor;
import com.cyanspring.server.validation.ParentOrderDefaultValueFiller;
import com.cyanspring.server.validation.ParentOrderPreCheck;
import com.cyanspring.server.validation.ParentOrderValidator;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;
import com.cyanspring.strategy.singleinstrument.SingleInstrumentStrategy;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class BusinessManager implements ApplicationContextAware {
	private static final Logger log = LoggerFactory
			.getLogger(BusinessManager.class);
	
	private ApplicationContext applicationContext;
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	@Autowired
	DownStreamManager downStreamManager;
	
	@Autowired
	IStrategyFactory strategyFactory;

	@Autowired
	private GenericDataConverter dataConverter;
	
	@Autowired
	private ParentOrderValidator parentOrderValidator;
	
	@Autowired
	ParentOrderPreCheck parentOrderPreCheck;
	
	@Autowired
	private ParentOrderDefaultValueFiller parentOrderDefaultValueFiller;
	
	@Autowired
	RefDataManager refDataManager;

	@Autowired
	TickTableManager tickTableManager;
	
	@Autowired
	private DefaultStartEndTime defaultStartEndTime;
	
	@Autowired
	GlobalStrategySettings globalStrategySettings;

	private int noOfContainers = 20;
	private ArrayList<IStrategyContainer> containers = new ArrayList<IStrategyContainer>();
	private Map<String, ParentOrder> orders = new HashMap<String, ParentOrder>();
	private boolean autoStartStrategy;
	
	
	public boolean isAutoStartStrategy() {
		return autoStartStrategy;
	}

	public void setAutoStartStrategy(boolean autoStartStrategy) {
		this.autoStartStrategy = autoStartStrategy;
	}

	public BusinessManager() {
	}
	
	AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			subscribeToEvent(InitClientRequestEvent.class, null);
			subscribeToEvent(EnterParentOrderEvent.class, null);
			subscribeToEvent(AmendParentOrderEvent.class, null);
			subscribeToEvent(CancelParentOrderEvent.class, null);
			subscribeToEvent(NewSingleInstrumentStrategyEvent.class, null);
			subscribeToEvent(NewMultiInstrumentStrategyEvent.class, null);
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
		
	};
	
	public void processEnterParentOrderEvent(EnterParentOrderEvent event) throws Exception {
		Map<String, Object> fields = event.getFields();

		boolean failed = false;
		String message = "";
		ParentOrder order = null;
		try {
			String strategyName = (String)fields.get(OrderField.STRATEGY.value());
			if(null == strategyName)
				throw new Exception("Strategy Field is missing");

			HashMap<String, Object> map = null;
			// pre-fill any fields that are specified
			parentOrderDefaultValueFiller.fillDefaults(fields);

			// check parameters are presented and types are well defined
			parentOrderPreCheck.validate(fields, null);
			
			// convert string presentation into object
			map = convertOrderFields(fields, strategyName);

			// validate all parameters have valid values
			parentOrderValidator.validate(map, null);

			// stick in the txId for future updates
			if(null != event.getTxId())
				map.put(OrderField.CLORDERID.value(), event.getTxId());
			
			// stick in source for FIX orders
			if(null != event.getKey())
				map.put(OrderField.SOURCE.value(), event.getKey());

			map.put(OrderField.IS_FIX.value(), event.isFix());
			
			order = new ParentOrder(map);

			if(orders.containsKey(order.getId())) {
				throw new OrderValidationException("Enter order: this order id already exists: " + order.getId());
			} 
			// add order to local map
			orders.put(order.getId(), order);
			
			// ack order
			EnterParentOrderReplyEvent reply = new EnterParentOrderReplyEvent(event.getKey(), event.getSender(), 
						true, "", event.getTxId(), order);

			eventManager.sendLocalOrRemoteEvent(reply);

			// send to order manager
			UpdateParentOrderEvent updateEvent = new UpdateParentOrderEvent(ExecType.NEW, event.getTxId(), order.clone(), null);
			eventManager.sendEvent(updateEvent);
			
			// create the strategy
			IDownStreamSender sender = downStreamManager.getSender();
			IStrategy strategy = strategyFactory.createStrategy(
					order.getStrategy(), 
					new Object[]{refDataManager, tickTableManager, order});
			strategy.setSender(sender);
			IStrategyContainer container = getLeastLoadContainer();
			log.debug("strategy " + strategy.getId() + " assigned to container " + container.getId());
			AddStrategyEvent addStrategyEvent = new AddStrategyEvent(container.getId(), strategy, true);
			eventManager.sendEvent(addStrategyEvent);
			
		} catch (OrderValidationException e) {
			failed = true;
			message = e.getMessage();
		} catch (OrderException e) {
			failed = true;
			message = e.getMessage();
		} catch (DataConvertException e) {
			failed = true;
			message = "DataConvertException: " + e.getMessage();
		} catch (DownStreamException e) {
			failed = true;
			message = e.getMessage();
		} catch (StrategyException e) {
			failed = true;
			message = e.getMessage();
		} catch (Exception e) {
			failed = true;
			log.error(e.getMessage(), e);
			e.printStackTrace();
			message = "Enter order failed, please check server log";
		}
		
		if (failed) {
			log.debug("Enter order failed: " + message);
			EnterParentOrderReplyEvent replyEvent = new EnterParentOrderReplyEvent(
					event.getKey(), event.getSender(), false, message, event.getTxId(), order);
			eventManager.sendLocalOrRemoteEvent(replyEvent);
		}
	
	}

	public void processAmendParentOrderEvent(AmendParentOrderEvent event) throws Exception {
		log.debug("processAmendParentOrderEvent received: " + event.getTxId() + ", " + event.getFields());
		
		Map<String, Object> fields = event.getFields();
		
		boolean failed = false;
		String message = "";
		ParentOrder order = null;
		try {
			// check whether order is there
			String id = (String)fields.get(OrderField.ID.value());
			order = orders.get(id);
			if(null == order)
				throw new OrderException("Cant find this order id: " + id);
			
			String strategyName = order.getStrategy();
			// convert string presentation into object
			HashMap<String, Object> map = convertOrderFields(fields, strategyName);
			
			Map<String, Object> ofields = order.getFields();
			Map<String, Object> changes = new HashMap<String, Object>();
			
			List<String> amendableFields = strategyFactory.getStrategyAmendableFields(order.getStrategy());
			for(Entry<String, Object> entry: map.entrySet()) {
				if(!amendableFields.contains(entry.getKey())) {
					log.debug("Field change not in amendable fields, ignored: " + entry.getKey() + ", " + entry.getValue());
					continue;
				}
				Object oldValue = ofields.get(entry.getKey());
				if((null != entry.getValue() && oldValue == null) ||
				   (null == entry.getValue() && oldValue != null) ) {
					changes.put(entry.getKey(), entry.getValue());
				} else if(null != entry.getValue() && oldValue != null) {
					boolean add = false;
					try {
						add = !oldValue.equals(entry.getValue());
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						throw new OrderValidationException("Field " + entry.getKey() + " comparison exception: " + e.getMessage());
					}
					if(add)
						changes.put(entry.getKey(), entry.getValue());
				}
						   
			}
			log.debug("Fields being changed: " + changes);
			
			// validate all parameters have valid values
			parentOrderValidator.validate(changes, order);
			
			// order is already completed
			if(order.getOrdStatus().isCompleted()) {
				AmendParentOrderReplyEvent replyEvent = 
					new AmendParentOrderReplyEvent(event.getKey(), event.getSender(), false, "Order already completed", event.getTxId(), order);
				eventManager.sendEvent(replyEvent);
				return;
			}
			
			// Order already terminated, no longer managed by strategy
			if(order.getState().equals(StrategyState.Terminated)) {
				order.update(changes);
				order.touch();
				AmendParentOrderReplyEvent replyEvent = 
					new AmendParentOrderReplyEvent(event.getKey(), event.getSender(), true, null, event.getTxId(), order);
				eventManager.sendEvent(replyEvent);
				
				UpdateParentOrderEvent updateEvent = new UpdateParentOrderEvent(ExecType.REPLACE, event.getTxId(), order.clone(), null);
				eventManager.sendEvent(updateEvent);
				return;
			}
			
			AmendStrategyOrderEvent amendEvent = new AmendStrategyOrderEvent(id, event.getSender(), event.getTxId(), event.getKey(), changes);
			eventManager.sendEvent(amendEvent);
			
		} catch (OrderValidationException e) {
			failed = true;
			message = e.getMessage();
		} catch (OrderException e) {
			failed = true;
			message = e.getMessage();
		} catch (DataConvertException e) {
			failed = true;
			message = "DataConvertException: " + e.getMessage();
		} catch (Exception e) {
			failed = true;
			log.error(e.getMessage(), e);
			e.printStackTrace();
			message = "Amend order failed, please check server log";
		}
		
		if (failed) {
			log.debug("Amend order failed: " + message);
			AmendParentOrderReplyEvent replyEvent = new AmendParentOrderReplyEvent(
					event.getKey(), event.getSender(), false, message, event.getTxId(), order);
			eventManager.sendLocalOrRemoteEvent(replyEvent);

		}
	}
	
	public void processCancelParentOrderEvent(CancelParentOrderEvent event) throws Exception {
		log.debug("processCancelParentOrderEvent received: " + event.getTxId() + ", " + event.getOrderId());
		ParentOrder order = orders.get(event.getOrderId());
		if(null == order) {
			CancelParentOrderReplyEvent reply = 
				new CancelParentOrderReplyEvent(event.getKey(), event.getSender(), false, 
						"Cant find this order id: " + event.getOrderId(), event.getTxId(), order);
			eventManager.sendLocalOrRemoteEvent(reply);
			return;
		}
		
		if(order.getOrdStatus().isCompleted()) {
			CancelParentOrderReplyEvent reply = 
				new CancelParentOrderReplyEvent(event.getKey(), event.getSender(), false, 
						"Order already completed: " + order.getId(), event.getTxId(), order);
			eventManager.sendLocalOrRemoteEvent(reply);
			return;
		}
		
		if(order.getState().equals(StrategyState.Terminated)) {
			order.setOrdStatus(OrdStatus.CANCELED);
			CancelParentOrderReplyEvent reply = new CancelParentOrderReplyEvent(event.getKey(), event.getSender(), true, null, event.getTxId(), order);
			eventManager.sendLocalOrRemoteEvent(reply);
			
			UpdateParentOrderEvent update = new UpdateParentOrderEvent(ExecType.CANCELED, event.getTxId(), order.clone(), null);
			eventManager.sendEvent(update);
			return;
		}

		CancelStrategyOrderEvent cancel = new CancelStrategyOrderEvent(order.getId(), event.getReceiver(), event.getTxId(), event.getKey());
		eventManager.sendEvent(cancel);
	}

	private HashMap<String, Object> convertOrderFields(Map<String, Object> fields, String strategyName) throws DataConvertException, StrategyException {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		Map<String, FieldDef> fieldDefs = strategyFactory.getStrategyFieldDef(strategyName);
		if(null == fieldDefs)
			throw new DataConvertException("Cant find field definition for strategy: " + strategyName);
		for(Entry<String, Object> entry: fields.entrySet()) {
			Object obj;
			if(entry.getValue() instanceof String) {
				FieldDef fieldDef = fieldDefs.get(entry.getKey());
				if(fieldDef == null) { //not found in input fields
					log.debug("Ignore field not defined in input fields: " + entry.getKey());
					continue;
				}
				obj = dataConverter.fromString(fieldDef.getType(), entry.getKey(), (String)entry.getValue());
			} else { // upstream already converted it from string type
				obj = entry.getValue();
			}
			map.put(entry.getKey(), obj);
		}

		return map;
	}
	
	
	public void processNewMultiInstrumentStrategyEvent(
			NewMultiInstrumentStrategyEvent event) {
		// create the strategy
		boolean failed = false;
		String message = "";
		String strategyName = "";
		try {
			IDownStreamSender sender = downStreamManager.getSender();
			strategyName = (String)event.getStrategy().get(OrderField.STRATEGY.value());
			if(null == strategyName)
				throw new StrategyException("Strategy field not present in NewMultiInstrumentStrategyEvent");
			IStrategy strategy = strategyFactory.createStrategy(
					strategyName, 
					new Object[]{refDataManager, tickTableManager, event.getStrategy(), event.getInstruments()});
			strategy.setSender(sender);
			IStrategyContainer container = getLeastLoadContainer();
			log.debug("strategy " + strategy.getId() + " assigned to container " + container.getId());
			AddStrategyEvent addStrategyEvent = new AddStrategyEvent(container.getId(), strategy, true);
			eventManager.sendEvent(addStrategyEvent);
		} catch (Exception e) {
			message = strategyName + " " + e.getMessage();
			log.error(message, e);
			failed = true;
		} 
		
		NewMultiInstrumentStrategyReplyEvent replyEvent = new NewMultiInstrumentStrategyReplyEvent(
				event.getKey(), event.getSender(), event.getTxId(), !failed, message);
		try {
			eventManager.sendLocalOrRemoteEvent(replyEvent);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	public void processNewSingleInstrumentStrategyEvent(NewSingleInstrumentStrategyEvent event) {
		// create the strategy
		boolean failed = false;
		String message = "";
		String strategyName = "";
		try {
			IDownStreamSender sender = downStreamManager.getSender();
			strategyName = (String)event.getInstrument().get(OrderField.STRATEGY.value());
			if(null == strategyName)
				throw new StrategyException("Strategy field not present in NewSingleInstrumentStrategyEvent");
			IStrategy strategy = strategyFactory.createStrategy(
					strategyName, 
					new Object[]{refDataManager, tickTableManager, event.getInstrument()});
			strategy.setSender(sender);
			IStrategyContainer container = getLeastLoadContainer();
			log.debug("strategy " + strategy.getId() + " assigned to container " + container.getId());
			AddStrategyEvent addStrategyEvent = new AddStrategyEvent(container.getId(), strategy, true);
			eventManager.sendEvent(addStrategyEvent);
		} catch (Exception e) {
			message = strategyName + " " + e.getMessage();
			log.error(message, e);
			failed = true;
		} 
		
		NewSingleInstrumentStrategyReplyEvent replyEvent = new NewSingleInstrumentStrategyReplyEvent(
				event.getKey(), event.getSender(), event.getTxId(), !failed, message);
		try {
			eventManager.sendLocalOrRemoteEvent(replyEvent);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	
	}
	

	private IStrategyContainer getLeastLoadContainer() {
		IStrategyContainer result = null;
		for(IStrategyContainer container: containers) {
			if(result == null || container.getStrategyCount() < result.getStrategyCount())
				result = container;
		}
		
		return result;
	}
	
	public int getNoOfContainers() {
		return noOfContainers;
	}

	public void setNoOfContainers(int noOfContainers) {
		this.noOfContainers = noOfContainers;
	}


	public void processInitClientRequestEvent(InitClientRequestEvent request) throws Exception {
		log.debug("Received InitClientRequestEvent from client: " + request.getSender());
		InitClientEvent event = new InitClientEvent(
				null, request.getSender(), 
				getSingleOrderDisplayFields(),
				getSingleOrderFieldDefs(), 
				getSingleInstrumentDisplayFields(),
				getSingleInstrumentFieldDefs(), 
				getMultiInstrumentDisplayFields(), 
				getMultiInstrumentFieldDefs(), 
				defaultStartEndTime);
		
		eventManager.sendRemoteEvent(event);
	}
	
	public List<String> getSingleOrderDisplayFields() throws StrategyException {
		List<String> result = globalStrategySettings.getSingleOrderCommonDisplayFields();
		if(null == result)
			throw new StrategyException("SingleOrderDisplayFields is null");
		return result;
	}

	public List<String> getSingleInstrumentDisplayFields() throws StrategyException {
		List<String> result = globalStrategySettings.getSingleInstrumentCommonDisplayFields();
		if(null == result)
			throw new StrategyException("SingleInstrumentDisplayFields is null");
		return result;
	}

	public List<String> getMultiInstrumentDisplayFields() throws StrategyException {
		List<String> result = globalStrategySettings.getMultiInstrumentCommonDisplayFields();
		if(null == result)
			throw new StrategyException("MultiInstrumentDisplayFields is null");
		return result;
	}

	private  Map<String, Map<String, FieldDef>> getSingleOrderFieldDefs() throws StrategyException {
		List<IStrategy> templates = strategyFactory.getAllStrategyTemplates();
		Map<String, Map<String, FieldDef>> result = new HashMap<String, Map<String, FieldDef>>();
		for(IStrategy template: templates) {
			if(template instanceof SingleOrderStrategy) {
				Map<String, FieldDef> fieldDefs = template.getCombinedFieldDefs();
				result.put(template.getStrategyName(), fieldDefs);
			}
		}
		return result;
	}
	
	private  Map<String, Map<String, FieldDef>> getSingleInstrumentFieldDefs() throws StrategyException {
		List<IStrategy> templates = strategyFactory.getAllStrategyTemplates();
		Map<String, Map<String, FieldDef>> result = new HashMap<String, Map<String, FieldDef>>();
		for(IStrategy template: templates) {
			if(template instanceof SingleInstrumentStrategy) {
				Map<String, FieldDef> fieldDefs = template.getCombinedFieldDefs();
				result.put(template.getStrategyName(), fieldDefs);
			}
		}
		return result;
	}
	
	synchronized public Map<String, MultiInstrumentStrategyDisplayConfig> getMultiInstrumentFieldDefs() throws StrategyException {
		List<IStrategy> templates = strategyFactory.getAllStrategyTemplates();
		Map<String, MultiInstrumentStrategyDisplayConfig> result = new HashMap<String, MultiInstrumentStrategyDisplayConfig>();
		for(IStrategy template: templates) {
			if(template instanceof MultiInstrumentStrategy) {
				MultiInstrumentStrategy ms = (MultiInstrumentStrategy)template;
				Map<String, FieldDef> fieldDefs = ms.getCombinedFieldDefs();
				List<String> instrumentDisplayFields = ms.getInstrumentDisplayFields();
				Map<String, FieldDef> instrumentLevelFieldDefs = ms.getCombinedInstrumentFieldDefs();
				MultiInstrumentStrategyDisplayConfig config = new MultiInstrumentStrategyDisplayConfig(
						ms.getStrategyName(),
						fieldDefs,
						instrumentDisplayFields,
						instrumentLevelFieldDefs
					);
				result.put(ms.getStrategyName(), config);
			}
		}
		return result;
	}

	
	public void injectStrategies(List<DataObject> list) {
		// create running strategies and assign to containers
		for(DataObject obj: list) {
			try {
				StrategyState state = obj.get(StrategyState.class, OrderField.STATE.value());
				if(state.equals(StrategyState.Terminated))
					continue;
				
				String strategyName = obj.get(String.class, OrderField.STRATEGY.value());
				IDownStreamSender sender = downStreamManager.getSender();
				IStrategy strategy;
				strategy = strategyFactory.createStrategy(strategyName, new Object[]{refDataManager, tickTableManager, obj});
				strategy.setSender(sender);
				IStrategyContainer container = getLeastLoadContainer();
				log.debug("strategy " + strategy.getId() + " assigned to container " + container.getId());
				if(strategy instanceof SingleOrderStrategy) {
					ParentOrder parentOrder = ((SingleOrderStrategy)strategy).getParentOrder();
					orders.put(parentOrder.getId(), parentOrder);
				}
				AddStrategyEvent addStrategyEvent = new AddStrategyEvent(container.getId(), strategy, autoStartStrategy);
				eventManager.sendEvent(addStrategyEvent);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} 
		}
	}
	
	public void init() throws Exception {
		strategyFactory.init();
		
		for(int i=0; i<noOfContainers; i++) {
			IStrategyContainer container = (IStrategyContainer)applicationContext.getBean("strategyContainer");
			container.init();
			containers.add(container);
		}

		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("BusinessManager");
	}

	public void uninit() {
		eventProcessor.uninit();

	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public boolean isSync() {
		return eventProcessor.isSync();
	}

	public void setSync(boolean sync) {
		eventProcessor.setSync(sync);
	}
	
}
