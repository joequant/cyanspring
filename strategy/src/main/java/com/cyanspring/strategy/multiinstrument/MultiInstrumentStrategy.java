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
package com.cyanspring.strategy.multiinstrument;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.AlertType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.alert.ClearMultiAlertEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.marketdata.TradeEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.ManualActionReplyEvent;
import com.cyanspring.common.event.order.ManualAmendChildOrderEvent;
import com.cyanspring.common.event.order.ManualCancelChildOrderEvent;
import com.cyanspring.common.event.order.ManualNewChildOrderEvent;
import com.cyanspring.common.event.strategy.AmendMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.AmendMultiInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.CancelMultiInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.ExecuteEvent;
import com.cyanspring.common.event.strategy.ExecutionInstructionResponseEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.MultiInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.PauseStrategyEvent;
import com.cyanspring.common.event.strategy.StartStrategyEvent;
import com.cyanspring.common.event.strategy.StopStrategyEvent;
import com.cyanspring.common.event.strategy.StrategyEndTimerEvent;
import com.cyanspring.common.event.strategy.StrategyStartTimerEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionEvent;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.staticdata.TickTableManager;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.ExecutionInstruction;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderAction;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.OrderUtils;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.Strategy;
import com.cyanspring.strategy.utils.QuoteUtil;


public abstract class MultiInstrumentStrategy extends Strategy {
	private static final Logger log = LoggerFactory
		.getLogger(MultiInstrumentStrategy.class);
	
	private IMultiInstrumentAnalyzer analyzer;
	protected MultiInstrumentStrategyData data;
	protected Map<String, Quote> quotes = new HashMap<String, Quote>();
	protected Map<String, Quote> adjQuotes = new HashMap<String, Quote>();
	protected RefDataManager refDataManager;
	protected TickTableManager tickTableManager;
	private MultiInstrumentFieldConverter fieldConverter = new MultiInstrumentFieldConverter(); 

	private static List<FieldDef> commonFieldDefs;
	private static List<FieldDef> commonInstrumentFieldDefs;
	private List<FieldDef> instrumentFieldDefs;
	private Map<String, FieldDef> combinedInstrumentFieldDefs;
	private List<String> instrumentDisplayFields;

	public Map<String, FieldDef> getCombinedInstrumentFieldDefs() throws StrategyException {
		if(null == combinedInstrumentFieldDefs)
			combinedInstrumentFieldDefs = combineFieldDefs(commonInstrumentFieldDefs, instrumentFieldDefs);
		return combinedInstrumentFieldDefs;
	}
	
	@Override
	protected Logger getLog() {
		return log;
	}

	@Override
	public String getId() {
		return this.data.getId();
	}

	@Override
	public void init() throws StrategyException {
		logDebug("Initialize strategy: " + getStrategyName() + ", " + this.getId());
		super.init();
		container.subscribe(ExecutionInstructionResponseEvent.class, getId(), this);
		container.subscribe(QuoteReplyEvent.class, getId(), this);
		container.subscribe(PauseStrategyEvent.class, getId(), this);
		container.subscribe(StopStrategyEvent.class, getId(), this);
		container.subscribe(StartStrategyEvent.class, getId(), this);
		container.subscribe(ManualCancelChildOrderEvent.class, getId(), this);
		container.subscribe(ManualNewChildOrderEvent.class, getId(), this);
		container.subscribe(ManualAmendChildOrderEvent.class, getId(), this);
		container.subscribe(ChildOrderUpdateEvent.class, getId(), this);
		container.subscribe(AmendMultiInstrumentStrategyEvent.class, getId(), this);
		container.subscribe(CancelMultiInstrumentStrategyEvent.class, getId(), this);
		container.subscribe(ExecuteEvent.class, getId(), this);
		container.subscribe(ClearMultiAlertEvent.class, getId(), this);
		container.subscribe(MarketSessionEvent.class, null, this);
		for(Instrument instr: data.getInstrumentData().values()) {
			container.subscribe(QuoteEvent.class, instr.getSymbol(), this);
			QuoteSubEvent quoteSubEvent = new QuoteSubEvent(getId(), null, instr.getSymbol());
			container.sendEvent(quoteSubEvent);
		}
		container.scheduleRepeatTimerEvent(getTimerInterval(), this, timerEvent);
		setStartEndTimer();
	}

	@Override
	public void uninit() {
		logDebug("uninit strategy: " + this.getId());
		executionManager.uninit();
		container.unsubscribe(ExecutionInstructionResponseEvent.class, getId(), this);
		container.unsubscribe(QuoteReplyEvent.class, getId(), this);
		container.unsubscribe(PauseStrategyEvent.class, getId(), this);
		container.unsubscribe(StopStrategyEvent.class, getId(), this);
		container.unsubscribe(StartStrategyEvent.class, getId(), this);
		container.unsubscribe(ManualCancelChildOrderEvent.class, getId(), this);
		container.unsubscribe(ManualNewChildOrderEvent.class, getId(), this);
		container.unsubscribe(ManualAmendChildOrderEvent.class, getId(), this);
		container.unsubscribe(ChildOrderUpdateEvent.class, getId(), this);
		container.unsubscribe(AmendMultiInstrumentStrategyEvent.class, getId(), this);
		container.unsubscribe(CancelMultiInstrumentStrategyEvent.class, getId(), this);
		container.unsubscribe(ExecuteEvent.class, getId(), this);
		container.unsubscribe(MarketSessionEvent.class, null, this);
		container.unsubscribe(ClearMultiAlertEvent.class, getId(), this);
		for(Instrument instr: data.getInstrumentData().values()) {
			container.unsubscribe(QuoteEvent.class, instr.getSymbol(), this);
		}
		container.cancelTimerEvent(timerEvent);
		cancelStartEndTimer();
	}
	
	/* 
	 * Expected parameters:
	 * 		RefDataManager
	 * 		TickTableManager
	 * 		MultiInstrumentStrategyData or Map<String, Object>
	 * 		(List<Map<String, Object>>)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void create(Object... objects) throws StrategyException {
		setRefDataManager((RefDataManager)objects[0]);
		setTickTableManager((TickTableManager)objects[1]);

		if(objects[2] instanceof MultiInstrumentStrategyData) {
			construct((MultiInstrumentStrategyData)objects[2]);
		} else {
			Map<String, Object> strategyLevelParams = (Map<String, Object>)objects[2];
			List<Map<String, Object>> instrumentLevelParams = (List<Map<String, Object>>)objects[3];
			construct(strategyLevelParams, instrumentLevelParams);
		}
	}

	@Override
	public RemoteAsyncEvent createConfigUpdateEvent() throws StrategyException {
			return new MultiInstrumentStrategyFieldDefUpdateEvent(null, null,
					new MultiInstrumentStrategyDisplayConfig(
							getStrategyName(), 
							getCombinedFieldDefs(), 
							getInstrumentDisplayFields(), 
							getCombinedInstrumentFieldDefs()));
	}
	
	// check compulsory strategy fields
	protected void checkCompulsoryStrategyFields(Map<String, Object> strategyLevelParams, 
			List<Map<String, Object>> instrumentLevelParams) throws StrategyException {
		for(Map<String, Object> instrumParams: instrumentLevelParams) {
			String symbol = (String)instrumParams.get(OrderField.SYMBOL.value());
			if(null == symbol)
				throw new StrategyException("Symbol is not specified for instrument level parameters");
			if(null == refDataManager.getRefData(symbol))
				throw new StrategyException("Can not find this symbol: " + symbol);
		}
		if(null != fieldConverter) {
			fieldConverter.checkCompulsoryStrategyFields(this, strategyLevelParams);
			fieldConverter.checkCompulsoryInstrumentFields(this, instrumentLevelParams);
		}
	}
	
	// strategy inherits from MultiInstrumentStrategy may override this to for extra construction step
	public void construct(Map<String, Object> strategyLevelParams, 
			List<Map<String, Object>> instrumentLevelParams) throws StrategyException {
		if(null == fieldConverter) {
			throw new StrategyException("Field converter is not specified");
		}
		
		// validate the basic
		checkCompulsoryStrategyFields(strategyLevelParams, instrumentLevelParams);
		
		// convert parameters
		try {
			strategyLevelParams = fieldConverter.convertStrategyFields(this, strategyLevelParams);
			instrumentLevelParams = fieldConverter.convertInstrumentFields(this, instrumentLevelParams);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new StrategyException(e.getMessage());
		}
		
		// construct strategy level parameters
		String strategyId = IdGenerator.getInstance().getNextID() + "S";
		strategyLevelParams.put(OrderField.ID.value(), strategyId);
		strategyLevelParams.put(OrderField.STATE.value(), StrategyState.Paused);
		this.data = new MultiInstrumentStrategyData(strategyLevelParams);
		
		// construct instrument level parameters
		for(Map<String, Object> map: instrumentLevelParams) {
			String instrumentId = IdGenerator.getInstance().getNextID() + "P";
			map.put(OrderField.ID.value(), instrumentId);
			map.put(OrderField.STRATEGY_ID.value(), strategyId);
			String symbol = (String)map.get(OrderField.SYMBOL.value());
			Instrument instr = new Instrument(symbol);
			instr.update(map);
			this.data.getInstrumentData().put(instrumentId, instr);
		}
		
		validate();
	}
	
	public void construct(MultiInstrumentStrategyData data) throws StrategyException {
		this.data = data;
		validate();
	}
	
	protected void validate() throws StrategyException {
		for(Instrument instr: this.data.getInstrumentData().values()) {
			RefData refData = refDataManager.getRefData(instr.getSymbol());
			if(null == refData)
				throw new StrategyException("Symbol not found: " + instr.getSymbol());
		}
	}
	
	public void sendStrategyDataUpdate() {
		MultiInstrumentStrategyUpdateEvent update = new MultiInstrumentStrategyUpdateEvent(null, null, (MultiInstrumentStrategyData)data.clone()); 
		container.sendEvent(update);
	}
	
	// send alert on strategy level
	public void setAlert(AlertType alertType, String msg) {
		this.data.put(OrderField.ALERT_TYPE.value(), alertType);
		this.data.put(OrderField.ALERT_MSG.value(), msg);
		sendStrategyDataUpdate();
	}
	
	public void setInstrumentAlert(Instrument instr, AlertType alertType, String msg) {
		instr.put(OrderField.ALERT_TYPE.value(), alertType);
		instr.put(OrderField.ALERT_MSG.value(), msg);
		AlertType es = data.get(AlertType.class, OrderField.ALERT_TYPE.value());
		if(null == es || alertType.compareTo(es) >=0) { // overwrite strategy level alert
			this.data.put(OrderField.ALERT_TYPE.value(), alertType);
			this.data.put(OrderField.ALERT_MSG.value(), msg);
		}
	}

	public void clearAlert() {
		data.remove(OrderField.ALERT_TYPE.value());
		data.remove(OrderField.ALERT_MSG.value());
		for(Instrument instr: data.getInstrumentData().values()) {
			instr.remove(OrderField.ALERT_TYPE.value());
			instr.remove(OrderField.ALERT_MSG.value());
		}
		sendStrategyDataUpdate();
	}

	public void clearAlert(Instrument instr) {
		instr.remove(OrderField.ALERT_TYPE.value());
		instr.remove(OrderField.ALERT_MSG.value());
		boolean found = false;
		for(Instrument i: data.getInstrumentData().values()) {
			if(i.fieldExists(OrderField.ALERT_TYPE.value())) {
				found = true;
			}
		}
		if(!found) { // if all alert cleared on instrument level, clear strategy level alert as well
			data.remove(OrderField.ALERT_TYPE.value());
			data.remove(OrderField.ALERT_MSG.value());
		}
		sendStrategyDataUpdate();
	}
	//

	@Override
	public void start() {
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void terminate() {
		super.terminate();
	}

	@Override
	public void execute(ExecuteTiming timing) {
		updateExecuteTiming(timing);
		
		if(pendingExecInstrEvent != null) {
			timeOutCheck();
			return;
		}
		
		if(getState().equals(StrategyState.Terminated) || 
		   getState().equals(StrategyState.Error) ||
		   getState().equals(StrategyState.Stopped))
							return;
						
		if(getState().equals(StrategyState.Stopping)) {
			if(childOrderCount() == 0)
				updateStrategyState(StrategyState.Stopped);
			else
				withDrawAllChildOrders();
			return;
		}

		if(checkStartEndTime())
			return;
		
		if(getState().equals(StrategyState.Paused) && pendingAckHandler == null)
			return;
			
		if(!preCheck())
			return;
		
		
		if(!checkQuotes())
			return;
			
		setLastExecuted(Clock.getInstance().now());
		setExecuteTiming(ExecuteTiming.NORMAL);
		
		PriceInstruction pi = analyze();
		List<ExecutionInstruction> eis = executionAnalyzer.analyze(pi, this);
		
		if(pendingAckHandler != null)
			eis = pendingAckHandler.preHandle(eis);
		
		if(eis != null && eis.size()>0) { // to reduce logging, only log when it comes up some changes
			logInfo("Open orders: " + childOrderList());
			logInfo("PriceInstruction: " + pi);
			logInfo("ExecutionInstruction: " + eis);
		} else {
			logDebug("Open orders: " + childOrderList());
			logDebug("PriceInstruction: " + pi);
			logDebug("ExecutionInstruction: " + eis);
		}

		executeInstructions(eis);
	}
	
	private String childOrderList() {
		StringBuilder sb = new StringBuilder();
		for(ChildOrder child: getChildOrders()) {
			sb.append("\n" + child);
		}
		return sb.toString();
	}
	
	protected PriceInstruction analyze() {
		if(analyzer != null)
			return analyzer.analyze(this);
		
		return null;
	}

	@Override
	public void onEvent(AsyncEvent event) {
		if (event instanceof AsyncTimerEvent) {
			processAsyncTimerEvent((AsyncTimerEvent)event);
		}else if (event instanceof ExecutionInstructionResponseEvent) {
			processExecutionInstructionResponseEvent((ExecutionInstructionResponseEvent)event);
		} else if (event instanceof QuoteEvent) {
			processQuoteEvent((QuoteEvent)event);
		} else if (event instanceof QuoteReplyEvent) {
			processQuoteReplyEvent((QuoteReplyEvent)event);
		} else if (event instanceof TradeEvent) {
			processTradeEvent((TradeEvent)event);
		} else if (event instanceof PauseStrategyEvent) {
			processPauseStrategyEvent((PauseStrategyEvent)event);
		} else if (event instanceof StopStrategyEvent) {
			processStopStrategyEvent((StopStrategyEvent)event);
		} else if (event instanceof StartStrategyEvent) {
			processStartStrategyEvent((StartStrategyEvent)event);
		} else if (event instanceof ChildOrderUpdateEvent) {
			processChildOrderUpdateEvent((ChildOrderUpdateEvent)event);
		} else if (event instanceof ManualCancelChildOrderEvent) {
			processManualCancelChildOrderEvent((ManualCancelChildOrderEvent)event);
		} else if (event instanceof ManualAmendChildOrderEvent) {
			processManualAmendChildOrderEvent((ManualAmendChildOrderEvent)event);
		} else if (event instanceof ManualNewChildOrderEvent) {
			processManualNewChildOrderEvent((ManualNewChildOrderEvent)event);
		} else if (event instanceof AmendMultiInstrumentStrategyEvent) {
			processAmendMultiInstrumentStrategyEvent((AmendMultiInstrumentStrategyEvent)event);
		} else if (event instanceof CancelMultiInstrumentStrategyEvent) {
			processCancelMultiInstrumentStrategyEvent((CancelMultiInstrumentStrategyEvent)event);
		} else if (event instanceof MarketSessionEvent) {
			processMarketSessionEvent((MarketSessionEvent)event);
		} else if (event instanceof ExecuteEvent) {
			processExecuteEvent((ExecuteEvent)event);
		} else if (event instanceof StrategyStartTimerEvent) {
			processStrategyStartTimerEvent((StrategyStartTimerEvent)event);
		} else if (event instanceof StrategyEndTimerEvent) {
			processStrategyEndTimerEvent((StrategyEndTimerEvent)event);
		} else if (event instanceof ClearMultiAlertEvent) {
			processClearMultiAlertEvent((ClearMultiAlertEvent)event);
		} else {
			log.warn("Unhandled event: " + event);
		}
	}

	private void processClearMultiAlertEvent(ClearMultiAlertEvent event) {
		if(event.getInstrKey() == null) // clear all
			clearAlert();
		else {
			Instrument instr = data.getInstrumentData().get(event.getInstrKey());
			clearAlert(instr);
		}
	}

	class CancelPendingHandler extends PendingAckHandler {
		CancelMultiInstrumentStrategyEvent event;
		public CancelPendingHandler(CancelMultiInstrumentStrategyEvent event) {
			this.event = event;
		}

		@Override
		public void postHandle(boolean success, String info) {
			if(success){
				terminate();
			}
			CancelMultiInstrumentStrategyReplyEvent reply = new CancelMultiInstrumentStrategyReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(), success, info);
			container.sendLocalOrRemoteEvent(reply);
			logDebug("CancelMultiInstrumentStrategyReplyEvent sent: " + reply.getTxId() + ", " + reply.isSuccess());
		}

		@Override
		public List<ExecutionInstruction> preHandle(List<ExecutionInstruction> eis) {
			// remove all child orders
			PriceInstruction pi = new PriceInstruction();
			List<ExecutionInstruction> ei = executionAnalyzer.analyze(pi, MultiInstrumentStrategy.this);
			logInfo("CancelPendingHandler has changed executionInstruction: " + ei);
			return ei;
		}
		
	}

	private void processCancelMultiInstrumentStrategyEvent(
			CancelMultiInstrumentStrategyEvent event) {
		logDebug("processCancelMultiInstrumentStrategyEvent received: " + event.getKey());
		CancelMultiInstrumentStrategyReplyEvent reply = null;
		if( pendingAckHandler != null ) {
			reply = new CancelMultiInstrumentStrategyReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(), false, "Stratey has amendment/cancellation");
			container.sendLocalOrRemoteEvent(reply);
			return;
		} 
		
		if(pendingExecInstrEvent == null && childOrderCount() == 0) {
			reply = new CancelMultiInstrumentStrategyReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(), true, "");
			container.sendLocalOrRemoteEvent(reply);
			terminate();
			return;
		}
		
		pendingAckHandler = new CancelPendingHandler(event);
		execute(ExecuteTiming.NOW);
	}

	private void processAmendMultiInstrumentStrategyEvent(
			AmendMultiInstrumentStrategyEvent event) {
		logDebug("AmendMultiInstrumentStrategyEvent: " + event.getKey());
		Map<String, Object> strategyLevelParams = null;
		List<Map<String, Object>> instrumentLevelParams = null;
		boolean failed = false;
		String message = "";
		try {
			if(null != event.getFields())
				strategyLevelParams = fieldConverter.convertStrategyFields(this, event.getFields());
			
			if(null != event.getInstruments())
				instrumentLevelParams = fieldConverter.convertInstrumentFields(this, event.getInstruments());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			failed = true;
			message = e.getMessage();
		}
		
		if(!failed) {
			MultiInstrumentStrategyData backup = (MultiInstrumentStrategyData) data.clone();// make a copy
			//default handling apply directly to the parameters
			if(null != strategyLevelParams)
				for(Entry<String, Object> entry: strategyLevelParams.entrySet()) {
					this.data.put(entry.getKey(), entry.getValue());
				}
			
			if(null != instrumentLevelParams)
				for(Map<String, Object> map: instrumentLevelParams) {
					String id = (String)map.get(OrderField.ID.value());
					Instrument instr = this.data.getInstrumentData().get(id);
					for(Entry<String, Object> entry: map.entrySet()) {
						instr.put(entry.getKey(), entry.getValue());
					}
				}
			
			try {
				validate();
			} catch (StrategyException e) {
				log.error(e.getMessage(), e);
				failed = true;
				message = e.getMessage();
				// roll back
				data = backup;
			}
		}
		AmendMultiInstrumentStrategyReplyEvent reply
			= new AmendMultiInstrumentStrategyReplyEvent(event.getKey(), event.getSender(), event.getTxId(), !failed, message);
		
		try {
			container.sendLocalOrRemoteEvent(reply);
			if(!failed) {
				sendStrategyDataUpdate();
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		executeWithTiming(ExecuteTiming.ASAP, AmendMultiInstrumentStrategyEvent.class);
	}

	protected void processManualCancelChildOrderEvent(ManualCancelChildOrderEvent event) {
		if(!checkPendingAck(event.getKey(), event.getSender())) {
			return;
		}
		
		List<ExecutionInstruction> ei = new ArrayList<ExecutionInstruction>();
		for(String childOrderId: event.getChildOrderIds()) {
			ChildOrder order = getChildOrder(childOrderId);
			if(order == null) {
				container.sendRemoteEvent(
						new ManualActionReplyEvent(null, event.getSender(), false, "cant find order in active child orders"));
				return;
			}
			ei.add(new ExecutionInstruction(OrderAction.CANCEL, order, null));
		}
		pendingAckHandler = new ManualPendingAckHandler(event.getKey(), event.getSender());
		executeInstructions(ei);
	}
	
	protected void processManualNewChildOrderEvent(ManualNewChildOrderEvent event) {
		if(!checkPendingAck(null, event.getSender())) {
			return;
		}
		
		Instrument instr = this.data.getInstrumentData().get(event.getParentId());
		List<ExecutionInstruction> ei = new ArrayList<ExecutionInstruction>();
		ChildOrder order = createChildOrder(event.getParentId(), instr.getSymbol(), event.getSide(),
				event.getQuantity(), event.getPrice(), event.getOrderType());
		ei.add(new ExecutionInstruction(OrderAction.NEW, order, null));
		pendingAckHandler = new ManualPendingAckHandler(event.getKey(), event.getSender());
		executeInstructions(ei);
	}
	
	protected void processManualAmendChildOrderEvent(
			ManualAmendChildOrderEvent event) {
		if(!checkPendingAck(event.getKey(), event.getSender())) {
			return;
		}

		List<ExecutionInstruction> ei = new ArrayList<ExecutionInstruction>();
		ChildOrder order = getChildOrder(event.getChildOrderId());
		if(order == null) {
			container.sendRemoteEvent(
					new ManualActionReplyEvent(null, event.getSender(), false, "cant find order in active child orders"));
			return;
		}
		
		if(PriceUtils.LessThan(event.getQuantity(), order.getCumQty())) {
			container.sendRemoteEvent(
					new ManualActionReplyEvent(null, event.getSender(), false, "CumQty is greater than intended quantity"));
			return;
		}

		Map<String, Object> changeFields = new HashMap<String, Object>();
		changeFields.put(OrderField.QUANTITY.value(), event.getQuantity());
		changeFields.put(OrderField.PRICE.value(), event.getPrice());
		ei.add(new ExecutionInstruction(OrderAction.AMEND, order, changeFields));
		pendingAckHandler = new ManualPendingAckHandler(event.getKey(), event.getSender());
		executeInstructions(ei);
	}
	
	private void calPnL() {
		double pnl = 0;
		for(Instrument instr: this.data.getInstrumentData().values()) {
			pnl += instr.getPnl();
		}
		this.data.setPnl(pnl);
	}

	protected void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		super.processChildOrderUpdateEvent(event);

		ChildOrder child = event.getOrder();
		if(event.getExecution() != null) {
			setLastExecutionTime(Clock.getInstance().now());
			Instrument instr = this.data.getInstrumentData().get(child.getParentOrderId());
			instr.processExecution(event.getExecution());
			calPnL();
			sendStrategyDataUpdate();
			logDebug("Strategy P/L: " + this.data.getPnl());
		}
		
		executeWithTiming(ExecuteTiming.ASAP, event.getClass());
	}
	

	protected void processPauseStrategyEvent(PauseStrategyEvent event) {
		pause();
		execute(ExecuteTiming.NOW);
	}
	
	protected void processStopStrategyEvent(StopStrategyEvent event) {
		stop();
		execute(ExecuteTiming.NOW);
	}
	
	protected void processStartStrategyEvent(StartStrategyEvent event) {
		start();
		execute(ExecuteTiming.ASAP);
	}
	

	protected void processTradeEvent(TradeEvent event) {
	}

	protected void processQuoteReplyEvent(QuoteReplyEvent event) {
		logDebug("QuoteReplyEvent: " + event.getQuote().toString());
		processQuote(event.getQuote());
	}
	
	@Override
	protected boolean checkCompletion() {
		return false;
	}

	public PriceInstruction flatPostionInstructions() {
		PriceInstruction pi = new PriceInstruction();
		for(Instrument instr: getData().getInstrumentData().values()) {
			double position = instr.getPosition();
			if(PriceUtils.isZero(position))
				continue;
			String symbol = instr.getSymbol();
			Quote quote = getQuote(symbol);
			OrderSide side = position>0?OrderSide.Sell:OrderSide.Buy;
			double qty = Math.abs(position);
			double price = QuoteUtil.getOppositePriceToQuantity(quote, qty, side);
			PriceAllocation pa = 
				new PriceAllocation(symbol, side, price, qty, ExchangeOrderType.LIMIT, instr.getId());
			pi.add(pa);
		}
		return pi;
	}
	
	public PriceAllocation flatInstrumentPosition(Instrument instr) {
		double position = instr.getPosition();
		if(PriceUtils.isZero(position))
			return null;
		String symbol = instr.getSymbol();
		Quote quote = getQuote(symbol);
		OrderSide side = position>0?OrderSide.Sell:OrderSide.Buy;
		double qty = Math.abs(position);
		double price = QuoteUtil.getOppositePriceToQuantity(quote, qty, side);
		return new PriceAllocation(symbol, side, price, qty, ExchangeOrderType.LIMIT, instr.getId());
	}
	
	
	protected boolean checkQuotes() {
		for(Instrument instr: this.data.getInstrumentData().values()) {
			Quote quote = quotes.get(instr.getSymbol());
			if(quote == null) {
				logDebug("Not all quotes are received");
				return false;
			}
			
			Quote adjQuote = adjQuotes.get(instr.getSymbol());
			if(adjQuote == null) {
				logDebug("Orders are not yet reflected in quotes");
				return false;
			}
			if(isValidateQuote() && !QuoteUtil.validateQuote(adjQuote)){
				return false;
			} 
		}
		
		return true;
	}
	
	protected void processQuoteEvent(QuoteEvent event) {
		logDebug("QuoteEvent: " + event.getQuote().toString());
		processQuote(event.getQuote());
		executeWithTiming(ExecuteTiming.ASAP, event.getClass());
	}
	
	protected void processQuote(Quote quote) {
		quotes.put(quote.getSymbol(), quote);
		
		Set<ChildOrder> children = getSortedOpenChildOrdersBySymbol(quote.getSymbol());
		Quote adjQuote;
		if(isCheckAdjQuote())
			adjQuote = OrderUtils.calAdjustedQuote(quote, children);
		else
			adjQuote = quote;
		
		if(adjQuote != null)
			adjQuotes.put(adjQuote.getSymbol(), adjQuote);
	}

	@Override
	public ChildOrder createChildOrder(String parentId, String symbol, OrderSide side,
			double quantity, double price, ExchangeOrderType type) {
		return new ChildOrder(symbol, side, quantity, price, type, parentId, this.getId());
	}

	@Override
	public Date getStartTime() {
		return this.data.get(Date.class, OrderField.START_TIME.value());
	}

	@Override
	public Date getEndTime() {
		return this.data.get(Date.class, OrderField.END_TIME.value());
	}
	
	@Override
	protected void updateStrategyState(StrategyState state) {
		setState(state);
		sendStrategyDataUpdate();
	}

	public boolean isZeroPosition() {
		for(Instrument instr: data.getInstrumentData().values()) {
			if(!PriceUtils.isZero(instr.getPosition()))
				return false;
		}
		return true;
	}
	
	public double getPnL() {
		double result = 0;
		for(Instrument instr: data.getInstrumentData().values()) {
			result += instr.getPnl();
		}
		return result;
	}

	public Quote getQuote(String symbol) {
		return quotes.get(symbol);
	}
	///////////////////////
	//
	// getters and setters
	//
	///////////////////////
	public RefDataManager getRefDataManager() {
		return refDataManager;
	}

	public void setRefDataManager(RefDataManager refDataManager) {
		this.refDataManager = refDataManager;
	}

	public IMultiInstrumentAnalyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(IMultiInstrumentAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	public TickTableManager getTickTableManager() {
		return tickTableManager;
	}

	public void setTickTableManager(TickTableManager tickTableManager) {
		this.tickTableManager = tickTableManager;
	}

	public MultiInstrumentFieldConverter getFieldConverter() {
		return fieldConverter;
	}

	public void setFieldConverter(MultiInstrumentFieldConverter fieldConverter) {
		this.fieldConverter = fieldConverter;
	}

	@Override
	public StrategyState getState() {
		return data.get(StrategyState.class, OrderField.STATE.value());
	}
	
	protected void setState(StrategyState state) {
		data.put(OrderField.STATE.value(), state);
	}

	public MultiInstrumentStrategyData getData() {
		return data;
	}

	public Map<String, Quote> getQuotes() {
		return quotes;
	}

	public Map<String, Quote> getAdjQuotes() {
		return adjQuotes;
	}

	@Override
	public List<FieldDef> getCommonFieldDefs() {
		return commonFieldDefs;
	}

	public static void setCommonFieldDefs(List<FieldDef> commonFieldDefs) {
		MultiInstrumentStrategy.commonFieldDefs = commonFieldDefs;
	}

	public static List<FieldDef> getCommonInstrumentFieldDefs() {
		return commonInstrumentFieldDefs;
	}

	public static void setCommonInstrumentFieldDefs(
			List<FieldDef> commonInstrumentFieldDefs) {
		MultiInstrumentStrategy.commonInstrumentFieldDefs = commonInstrumentFieldDefs;
	}

	public List<FieldDef> getInstrumentFieldDefs() {
		return instrumentFieldDefs;
	}

	public void setInstrumentFieldDefs(List<FieldDef> instrumentFieldDefs) {
		this.instrumentFieldDefs = instrumentFieldDefs;
	}

	public List<String> getInstrumentDisplayFields() {
		return instrumentDisplayFields;
	}

	public void setInstrumentDisplayFields(List<String> instrumentDisplayFields) {
		this.instrumentDisplayFields = instrumentDisplayFields;
	}
	
}
