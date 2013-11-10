package com.cyanspring.strategy.singleinstrument;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.AlertType;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.alert.ClearSingleAlertEvent;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.marketdata.QuoteReplyEvent;
import com.cyanspring.common.event.marketdata.QuoteSubEvent;
import com.cyanspring.common.event.marketdata.TradeEvent;
import com.cyanspring.common.event.order.AmendStrategyOrderEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.ManualActionReplyEvent;
import com.cyanspring.common.event.order.ManualAmendChildOrderEvent;
import com.cyanspring.common.event.order.ManualCancelChildOrderEvent;
import com.cyanspring.common.event.order.ManualNewChildOrderEvent;
import com.cyanspring.common.event.strategy.AmendSingleInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.CancelSingleInstrumentStrategyEvent;
import com.cyanspring.common.event.strategy.CancelSingleInstrumentStrategyReplyEvent;
import com.cyanspring.common.event.strategy.ExecuteEvent;
import com.cyanspring.common.event.strategy.ExecutionInstructionResponseEvent;
import com.cyanspring.common.event.strategy.PauseStrategyEvent;
import com.cyanspring.common.event.strategy.SingleInstrumentStrategyUpdateEvent;
import com.cyanspring.common.event.strategy.SingleOrderStrategyFieldDefUpdateEvent;
import com.cyanspring.common.event.strategy.StartStrategyEvent;
import com.cyanspring.common.event.strategy.StopStrategyEvent;
import com.cyanspring.common.event.strategy.StrategyEndTimerEvent;
import com.cyanspring.common.event.strategy.StrategyStartTimerEvent;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketsession.MarketSessionEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.staticdata.TickTableManager;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.ExecutionInstruction;
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

public class SingleInstrumentStrategy extends Strategy {
	private static final Logger log = LoggerFactory
			.getLogger(SingleInstrumentStrategy.class);
	
	protected Instrument instrument;
	protected Quote quote;
	protected Quote adjQuote;
	protected RefData refData;
	protected ITickTable tickTable;
	protected ISingleInstrumentAnalyzer analyzer;
	private static List<FieldDef> commonFieldDefs;
	private SingleInstrumentFieldConverter fieldConverter = new SingleInstrumentFieldConverter(); 
	
	protected Logger getLog() {
		return log;
	}
	
	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	@Override
	public StrategyState getState(){
		return instrument.get(StrategyState.class, OrderField.STATE.value());
	}
	
	private void setState(StrategyState state) {
		instrument.put(OrderField.STATE.value(), state);
	}
	
	protected void sendStrategyUpdate() {
		SingleInstrumentStrategyUpdateEvent updateEvent = new SingleInstrumentStrategyUpdateEvent(null, null, (Instrument)instrument.clone());
		container.sendEvent(updateEvent);
	}
	
	@Override
	public void init() throws StrategyException {
		logDebug("Initialize strategy: " + getStrategyName() + ", " + this.getId() + ", " + instrument.getSymbol());
		super.init();
		container.subscribe(ExecutionInstructionResponseEvent.class, instrument.getId(), this);
		container.subscribe(QuoteEvent.class, instrument.getSymbol(), this);
		container.subscribe(QuoteReplyEvent.class, instrument.getId(), this);
		container.subscribe(PauseStrategyEvent.class, instrument.getId(), this);
		container.subscribe(StopStrategyEvent.class, instrument.getId(), this);
		container.subscribe(StartStrategyEvent.class, instrument.getId(), this);
		container.subscribe(ManualCancelChildOrderEvent.class, instrument.getId(), this);
		container.subscribe(ManualNewChildOrderEvent.class, instrument.getId(), this);
		container.subscribe(ManualAmendChildOrderEvent.class, instrument.getId(), this);
		container.subscribe(ChildOrderUpdateEvent.class, instrument.getId(), this);
		container.subscribe(AmendSingleInstrumentStrategyEvent.class, instrument.getId(), this);
		container.subscribe(CancelSingleInstrumentStrategyEvent.class, instrument.getId(), this);
		container.subscribe(ExecuteEvent.class, instrument.getId(), this);
		container.subscribe(ClearSingleAlertEvent.class, instrument.getId(), this);
		container.subscribe(MarketSessionEvent.class, null, this);
		QuoteSubEvent quoteSubEvent = new QuoteSubEvent(instrument.getId(), null, instrument.getSymbol());
		container.sendEvent(quoteSubEvent);
		container.scheduleRepeatTimerEvent(getTimerInterval(), this, timerEvent);
		setStartEndTimer();
	}
	
	@Override
	public void uninit() {
		executionManager.uninit();
		container.unsubscribe(ExecutionInstructionResponseEvent.class, instrument.getId(), this);
		container.unsubscribe(QuoteEvent.class, instrument.getSymbol(), this);
		container.unsubscribe(QuoteReplyEvent.class, instrument.getId(), this);
		container.unsubscribe(PauseStrategyEvent.class, instrument.getId(), this);
		container.unsubscribe(StopStrategyEvent.class, instrument.getId(), this);
		container.unsubscribe(StartStrategyEvent.class, instrument.getId(), this);
		container.unsubscribe(ManualCancelChildOrderEvent.class, instrument.getId(), this);
		container.unsubscribe(ManualNewChildOrderEvent.class, instrument.getId(), this);
		container.unsubscribe(ManualAmendChildOrderEvent.class, instrument.getId(), this);
		container.unsubscribe(ChildOrderUpdateEvent.class, instrument.getId(), this);
		container.unsubscribe(AmendSingleInstrumentStrategyEvent.class, instrument.getId(), this);
		container.unsubscribe(CancelSingleInstrumentStrategyEvent.class, instrument.getId(), this);
		container.unsubscribe(ExecuteEvent.class, instrument.getId(), this);
		container.unsubscribe(MarketSessionEvent.class, null, this);
		container.unsubscribe(ClearSingleAlertEvent.class, instrument.getId(), this);
		container.cancelTimerEvent(timerEvent);
		cancelStartEndTimer();
	}

	
	/* 
	 * Expected parameters:
	 * 		RefDataManager
	 * 		TickTableManager
	 * 		ParentOrder
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void create(Object... objects) throws StrategyException {
		RefDataManager refDataManager = (RefDataManager)objects[0];
		TickTableManager tickTableManager = (TickTableManager)objects[1];
		
		if(objects[2] instanceof Instrument) {
			construct((Instrument)objects[2]);
		} else if(objects[2] instanceof Map<?, ?>){
			construct((Map<String, Object>)objects[2]);
		} else {
			throw new StrategyException("Wrong object type: " + objects[2].getClass());
		}

		RefData refData = refDataManager.getRefData(this.instrument.getSymbol());
		if(null == refData)
			throw new StrategyException("Can't find symbol in refdata: " + instrument.getSymbol());
		setRefData(refData);

		ITickTable tickTable = tickTableManager.getTickTable(instrument.getSymbol());
		if(null == tickTable)
			throw new StrategyException("Can't find tick table for: " + instrument.getSymbol());
		setTickTable(tickTable);
	}
	
	// strategy inherits from SingleInstrumentStrategy may override this to for extra construction step
	public void construct(Map<String, Object> instrumentParams) throws StrategyException {
		if(null == fieldConverter) {
			throw new StrategyException("Field converter is not specified");
		}
		
		// convert parameters
		try {
			instrumentParams = fieldConverter.convertInstrumentFields(this, instrumentParams);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new StrategyException(e.getMessage());
		}
		
		// construct strategy level parameters
		String strategyId = IdGenerator.getInstance().getNextID() + "S";
		instrumentParams.put(OrderField.ID.value(), strategyId);
		instrumentParams.put(OrderField.STATE.value(), StrategyState.Paused);
		this.instrument = new Instrument("");
		instrument.update(instrumentParams);
		
		validate();
	}
	
	public void construct(Instrument instrument) throws StrategyException {
		this.instrument = instrument;
		validate();
	}
	
	protected void validate() throws StrategyException {
	}
	
	// check compulsory strategy fields
	protected void checkCompulsoryStrategyFields(Map<String, Object> instrumentParams) throws StrategyException {
		String symbol = (String)instrumentParams.get(OrderField.SYMBOL.value());
		if(null == symbol)
			throw new StrategyException("Symbol is not specified for instrument level parameters");

		if(null != fieldConverter) {
			fieldConverter.checkCompulsoryInstrumentFields(this, instrumentParams);
		}
	}

	@Override
	protected boolean checkCompletion() {
		return false;
	}
	
	@Override
	public RemoteAsyncEvent createConfigUpdateEvent() throws StrategyException {
			return new SingleOrderStrategyFieldDefUpdateEvent(null, null, getStrategyName(), getCombinedFieldDefs());
	}
	
	@Override
	protected void updateStrategyState(StrategyState state) {
		setState(state);
		sendStrategyUpdate();		
	}
	
	public void setAlert(AlertType alertType, String msg) {
		instrument.put(OrderField.ALERT_TYPE.value(), alertType);
		instrument.put(OrderField.ALERT_MSG.value(), msg);
		sendStrategyUpdate();		
	}
	
	public void clearAlert() {
		instrument.remove(OrderField.ALERT_TYPE.value());
		instrument.remove(OrderField.ALERT_MSG.value());
		sendStrategyUpdate();		
	}

	@Override
	public ChildOrder createChildOrder(String parentId, String symbol, OrderSide side, double quantity, double price, ExchangeOrderType type) {
		//ignore id, symbol & side specification for single order strategy. They should be the same as parent orders.
		return instrument.createChildOrder(side, quantity, price, type);
	}

	protected PriceInstruction analyze() {
		return null;
	}
	
	@Override
	public void execute(ExecuteTiming timing) {
		updateExecuteTiming(timing);
		
		if(pendingExecInstrEvent != null) {
			timeOutCheck();
			return;
		}
		
		StrategyState state = getState();
		if(state.equals(StrategyState.Terminated) || 
			state.equals(StrategyState.Error) ||
			state.equals(StrategyState.Stopped))
							return;
						
		if(state.equals(StrategyState.Stopping)) {
			if(childOrderCount() == 0)
				updateStrategyState(StrategyState.Stopped);
			else
				withDrawAllChildOrders();
			return;
		}

		if(checkStartEndTime())
			return;
		
		if(state.equals(StrategyState.Paused) && pendingAckHandler == null)
			return;
			
		if(!preCheck())
			return;
		
		if(quote == null) {
			logDebug("not doing anything since we haven't recieved any quote yet");
			return;
		}
		
		Set<ChildOrder> children = getSortedOpenChildOrdersBySymbol(quote.getSymbol());
		if(isCheckAdjQuote())
			adjQuote = OrderUtils.calAdjustedQuote(quote, children);
		else 
			adjQuote = quote;
		
		if(adjQuote == null) {
			logDebug("Our orders are still not yet reflected in market image");
			return;
		} 
		
		if(isValidateQuote() && !QuoteUtil.validateQuote(adjQuote)){
			return;
		} 
		
		setLastExecuted(Clock.getInstance().now());
		setExecuteTiming(ExecuteTiming.NORMAL);
		
		//QPE framework
		PriceInstruction pi = null;
		if(null != analyzer ) {
			pi = analyzer.analyze(this);
		} else {
			pi = this.analyze();
		}
		List<ExecutionInstruction> eis = executionAnalyzer.analyze(pi, this);
		
		if(pendingAckHandler != null)
			eis = pendingAckHandler.preHandle(eis);
		
		if(eis != null && eis.size()>0) { // to reduce logging, only log when it comes up some changes
			logInfo("Strategy: " + "position: " + instrument.getPosition());
			logInfo("Open orders: " + children);
			logInfo("Quote: " + quote.toString());
			logInfo("AdjQuote: " + adjQuote.toString());
			logInfo("PriceInstruction: " + pi);
			logInfo("ExecutionInstruction: " + eis);
		} else {
			logDebug("Strategy: " + "position: " + instrument.getPosition());
			logDebug("Open orders: " + children);
			logDebug("Quote: " + quote.toString());
			logDebug("AdjQuote: " + adjQuote.toString());
			logDebug("PriceInstruction: " + pi);
			logDebug("ExecutionInstruction: " + eis);
		}

		executeInstructions(eis);
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
		
		List<ExecutionInstruction> ei = new ArrayList<ExecutionInstruction>();
		ChildOrder order = instrument.createChildOrder(event.getSide(), event.getQuantity(), event.getPrice(), event.getOrderType());
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

	protected void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		super.processChildOrderUpdateEvent(event);
		
		if(event.getExecution() != null) {
			setLastExecutionTime(Clock.getInstance().now());
			instrument.processExecution(event.getExecution());
			sendStrategyUpdate();
		}
		
		executeWithTiming(ExecuteTiming.ASAP, event.getClass());
	}
	
	protected void processClearSingleAlertEvent(ClearSingleAlertEvent event) {
		clearAlert();
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
		} else if (event instanceof AmendSingleInstrumentStrategyEvent) {
			processAmendSingleInstrumentStrategyEvent((AmendSingleInstrumentStrategyEvent)event);
		} else if (event instanceof CancelSingleInstrumentStrategyEvent) {
			processCancelSingleInstrumentStrategyEvent((CancelSingleInstrumentStrategyEvent)event);
		} else if (event instanceof MarketSessionEvent) {
			processMarketSessionEvent((MarketSessionEvent)event);
		} else if (event instanceof ExecuteEvent) {
			processExecuteEvent((ExecuteEvent)event);
		} else if (event instanceof StrategyStartTimerEvent) {
			processStrategyStartTimerEvent((StrategyStartTimerEvent)event);
		} else if (event instanceof StrategyEndTimerEvent) {
			processStrategyEndTimerEvent((StrategyEndTimerEvent)event);
		} else if (event instanceof ClearSingleAlertEvent) {
			processClearSingleAlertEvent((ClearSingleAlertEvent)event);
		} else {
			log.warn("Unhandled event: " + event);
		}
	}
	
	class CancelPendingHandler extends PendingAckHandler {
		CancelSingleInstrumentStrategyEvent event;
		public CancelPendingHandler(CancelSingleInstrumentStrategyEvent event) {
			this.event = event;
		}

		@Override
		public void postHandle(boolean success, String info) {
			if(success){
				terminate();
			}
			CancelSingleInstrumentStrategyReplyEvent reply = new CancelSingleInstrumentStrategyReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(), success, info);
			container.sendLocalOrRemoteEvent(reply);
			logDebug("CancelSingleInstrumentStrategyReplyEvent sent: " + reply.getTxId() + ", " + reply.isSuccess());
		}

		@Override
		public List<ExecutionInstruction> preHandle(List<ExecutionInstruction> eis) {
			// remove all child orders
			PriceInstruction pi = new PriceInstruction();
			List<ExecutionInstruction> ei = executionAnalyzer.analyze(pi, SingleInstrumentStrategy.this);
			logInfo("CancelPendingHandler has changed executionInstruction: " + ei);
			return ei;
		}
		
	}

	private void processCancelSingleInstrumentStrategyEvent(
			CancelSingleInstrumentStrategyEvent event) {
		logDebug("processCancelSingleInstrumentStrategyEvent received: " + event.getKey());
		CancelSingleInstrumentStrategyReplyEvent reply = null;
		if( pendingAckHandler != null ) {
			reply = new CancelSingleInstrumentStrategyReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(), false, "Stratey has amendment/cancellation pending");
			container.sendLocalOrRemoteEvent(reply);
			return;
		} 
		
		if(pendingExecInstrEvent == null && childOrderCount() == 0) {
			reply = new CancelSingleInstrumentStrategyReplyEvent(
					event.getKey(), event.getSender(), event.getTxId(), true, "");
			container.sendLocalOrRemoteEvent(reply);
			terminate();
			return;
		}
		
		pendingAckHandler = new CancelPendingHandler(event);
		execute(ExecuteTiming.NOW);
	}

	private void processAmendSingleInstrumentStrategyEvent(
			AmendSingleInstrumentStrategyEvent event) {
		Map<String, Object> fields = event.getFields();
		this.instrument.update(fields);
		if(fields.containsKey(OrderField.START_TIME.value()) ||
				fields.containsKey(OrderField.START_TIME.value()))
			setStartEndTimer();
		execute(ExecuteTiming.NOW);
	}

	protected void processPauseStrategyEvent(PauseStrategyEvent event) {
		log.debug("received PauseStrategyEvent");
		pause();
		execute(ExecuteTiming.NOW);
	}
	
	protected void processStopStrategyEvent(StopStrategyEvent event) {
		logDebug("received StopStrategyEvent");
		stop();
		execute(ExecuteTiming.NOW);
	}
	
	protected void processStartStrategyEvent(StartStrategyEvent event) {
		start();
		execute(ExecuteTiming.NOW);
	}
	
	protected void processQuoteEvent(QuoteEvent event) {
		logDebug("QuoteEvent: " + event.getQuote().toString());
		quote = event.getQuote();
		
		executeWithTiming(ExecuteTiming.ASAP, event.getClass());
	}

	protected void processQuoteReplyEvent(QuoteReplyEvent event) {
		logDebug("QuoteReplyEvent: " + event.getQuote().toString());
		quote = event.getQuote();
		
		executeWithTiming(ExecuteTiming.ASAP, event.getClass());
	}

	protected void processTradeEvent(TradeEvent event) {
	}
	
	///////////////////////
	//
	// getters and setters
	//
	///////////////////////
	public String getId() {
		return instrument.getId();
	}
	
	public Quote getQuote() {
		return quote;
	}


	public RefData getRefData() {
		return refData;
	}


	public void setRefData(RefData refData) {
		this.refData = refData;
	}

	public ITickTable getTickTable() {
		return tickTable;
	}

	public void setTickTable(ITickTable tickTable) {
		this.tickTable = tickTable;
	}

	public Quote getAdjQuote() {
		return adjQuote;
	}

	public MarketSessionType getMarketSession() {
		return marketSession;
	}

	@Override
	public Date getStartTime(){
		return instrument.get(Date.class, OrderField.START_TIME.value());
	}
	
	@Override
	public Date getEndTime() {
		return instrument.get(Date.class, OrderField.END_TIME.value());
	}
	
	@Override
	public List<FieldDef> getCommonFieldDefs() {
		return commonFieldDefs;
	}

	public static void setCommonFieldDefs(List<FieldDef> commonFieldDefs) {
		SingleInstrumentStrategy.commonFieldDefs = commonFieldDefs;
	}

	public SingleInstrumentFieldConverter getFieldConverter() {
		return fieldConverter;
	}

	public void setFieldConverter(SingleInstrumentFieldConverter fieldConverter) {
		this.fieldConverter = fieldConverter;
	}
	
}
