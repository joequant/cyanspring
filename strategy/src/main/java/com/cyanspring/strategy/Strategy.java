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
package com.cyanspring.strategy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import com.cyanspring.common.data.AlertType;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.AsyncTimerEvent;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.order.ChildOrderUpdateEvent;
import com.cyanspring.common.event.order.ManualActionReplyEvent;
import com.cyanspring.common.event.strategy.ExecuteEvent;
import com.cyanspring.common.event.strategy.ExecutionInstructionEvent;
import com.cyanspring.common.event.strategy.ExecutionInstructionResponseEvent;
import com.cyanspring.common.event.strategy.RemoveStrategyEvent;
import com.cyanspring.common.event.strategy.StrategyEndTimerEvent;
import com.cyanspring.common.event.strategy.StrategyLogEvent;
import com.cyanspring.common.event.strategy.StrategyStartTimerEvent;
import com.cyanspring.common.marketsession.MarketSessionEvent;
import com.cyanspring.common.marketsession.MarketSessionType;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.ExecutionInstruction;
import com.cyanspring.common.strategy.IExecutionAnalyzer;
import com.cyanspring.common.strategy.IExecutionManager;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.strategy.IStrategyContainer;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.LogType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.OrderUtils;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.util.TimeUtil;

public abstract class Strategy implements IStrategy, IAsyncExecuteEventListener {
	private static final Logger log = LoggerFactory
		.getLogger(Strategy.class);
	private String strategyName;
	protected IStrategyContainer container;
	protected StrategyState state;
	protected IExecutionManager executionManager;
	protected IExecutionAnalyzer executionAnalyzer;
	protected IDownStreamSender sender;
	protected SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	protected ExecutionInstructionEvent pendingExecInstrEvent;
	protected AsyncTimerEvent timerEvent = new AsyncTimerEvent();
	private long execPendingTimeOut = 10000; 
	private int execFailCount;
	private int execFailLimit = 3;
	private long lpInterval = 8000; // low priority timer buff for actions such as gc/maintenance
	private long hpInterval = 300;  // high priority timer buff for actions such as market data/execution 
	private ExecuteTiming executeTiming = ExecuteTiming.NORMAL;
	protected Map<Class<? extends AsyncEvent>, ExecuteTiming> eventExecuteTiming;
	private Date lastExecuted = Clock.getInstance().now();
	private Date lastActionTime = Clock.getInstance().now();
	private Date lastExecutionTime = Clock.getInstance().now();
	private int timerInterval = 500;
	private StrategyStartTimerEvent strategyStartTimerEvent = new StrategyStartTimerEvent();
	private StrategyEndTimerEvent strategyEndTimerEvent = new StrategyEndTimerEvent();
	private Map<String, ChildOrder> childOrders = new HashMap<String, ChildOrder>();
	protected PendingAckHandler pendingAckHandler;
	protected MarketSessionType marketSession;
	private boolean checkAdjQuote;
	private boolean validateQuote;
	
	private List<FieldDef> strategyFieldDefs;
	private Map<String, FieldDef> combineFieldDefs;
	
	@Override
	public abstract List<FieldDef> getCommonFieldDefs();
	
	@Override
	public abstract RemoteAsyncEvent createConfigUpdateEvent() throws StrategyException;
	
	@Override
	public Map<String, FieldDef> getCombinedFieldDefs() throws StrategyException {
		if(null == combineFieldDefs)
			combineFieldDefs = combineFieldDefs(getCommonFieldDefs(), strategyFieldDefs);
		return combineFieldDefs;
	}
	
	protected Map<String, FieldDef> combineFieldDefs(
			List<FieldDef> commonFieldDefs, List<FieldDef> strategyFieldDefs) throws StrategyException {
		if(null == commonFieldDefs) {
			throw new StrategyException("Common strategy field definition is not set");
		}
		
		Map<String, FieldDef> fieldDefs = new HashMap<String, FieldDef>();
		
		for(FieldDef fieldDef: commonFieldDefs) {
			FieldDef existing = fieldDefs.put(fieldDef.getName(), fieldDef);
			if(null != existing) {
				log.warn("Overwriting strategy field def: " + fieldDef);
			}
		}
		
		if(null != strategyFieldDefs) {
			for(FieldDef fieldDef: strategyFieldDefs) {
				FieldDef existing = fieldDefs.put(fieldDef.getName(), fieldDef);
				if(null != existing) {
					log.warn("Overwriting strategy field def: " + fieldDef);
				}
			}
		}
		
		return fieldDefs;
	}


	protected abstract class PendingAckHandler {
		String eiEventId;
		public void setPendingInstructionEventId(String eventId) {
			this.eiEventId = eventId;
			
		}
		public String getPendingInstructionEventId() {
			return eiEventId;
		}
		public abstract void postHandle(boolean success, String info);
		public abstract List<ExecutionInstruction> preHandle(List<ExecutionInstruction> eis);
	}
	
	protected class ManualPendingAckHandler extends PendingAckHandler {
		String sender;
		String id;
		
		public ManualPendingAckHandler(String id, String sender) {
			this.id = id;
			this.sender = sender;
		}

		@Override
		public void postHandle(boolean success, String info) {
			container.sendRemoteEvent(
					new ManualActionReplyEvent(id, sender, success, info));
		}

		@Override
		public List<ExecutionInstruction> preHandle(List<ExecutionInstruction> eis) {
			// nothing to do here
			return null;
		}
		
	}

	protected abstract Logger getLog(); 
	
	protected String clientMessage(String message) {
		return timeFormat.format(Clock.getInstance().now()) + ": " + message;
	}
	public void logDebug(String message) {
		getLog().debug( "[" + getId() + "] " + message);
	}
	public void logInfo(String message) {
		getLog().info( "[" + getId() + "] " + message);
		container.sendRemoteEvent(new StrategyLogEvent(getId(), null, LogType.Info, clientMessage(message)));
	}
	public void logWarn(String message) {
		getLog().warn( "[" + getId() + "] " + message);
		container.sendRemoteEvent(new StrategyLogEvent(getId(), null, LogType.Warn, clientMessage(message)));
		setAlert(AlertType.WARNING, message);
	}
	
	public void logError(String message) {
		getLog().error( "[" + getId() + "] " + message);
		container.sendRemoteEvent(new StrategyLogEvent(getId(), null, LogType.Error, clientMessage(message)));
		setAlert(AlertType.ERROR, message);
	}
	
	public abstract void setAlert(AlertType alertType, String msg);
	
	public Strategy() {
		super();
	}
	
	public StrategyState getState() {
		return state;
	}
	
	protected void setStartEndTimer() {
		Date start = getStartTime();
		Date end = getEndTime();
		Date now = Clock.getInstance().now();
		if(null == start) {
			container.cancelTimerEvent(strategyStartTimerEvent);
		}
		
		if(null == end) {
			container.cancelTimerEvent(strategyEndTimerEvent);
		}
		
		if(null != start && start.after(now)) {
			logDebug("Setting strategy start timer: " + start);
			container.scheduleTimerEvent(start, this, strategyStartTimerEvent);
		}
	
		if(null != end && end.after(now)) {
			logDebug("Setting strategy end timer: " + end);
			container.scheduleTimerEvent(end, this, strategyEndTimerEvent);
		}
	}
	
	protected void cancelStartEndTimer() {
		container.cancelTimerEvent(strategyStartTimerEvent);
		container.cancelTimerEvent(strategyEndTimerEvent);
	}
	
	protected boolean hasPendingChildOrder() {
		Set<ChildOrder> children = OrderUtils.getSortedOpenChildOrders(childOrders.values());
		for(ChildOrder child: children) {
			if(child.getOrdStatus().isPending()) {
				return true;
			}
		}
		return false;
	}

	public Collection<ChildOrder> getChildOrders() {
		return new ArrayList<ChildOrder>(childOrders.values());
	}

	public List<ChildOrder> getOpenChildOrdersByParent(String parent) {
		ArrayList<ChildOrder> list = new ArrayList<ChildOrder>();
		for(ChildOrder order: childOrders.values()) {
			if(order.getParentOrderId().equals(parent))
				list.add(order);
		}
		return list;
	}

	public Set<ChildOrder> getSortedOpenChildOrdersByParent(String parent) {
		ArrayList<ChildOrder> list = new ArrayList<ChildOrder>();
		for(ChildOrder order: childOrders.values()) {
			if(order.getParentOrderId().equals(parent))
				list.add(order);
		}
		return OrderUtils.getSortedOpenChildOrders(list);
	}
	
	public Set<ChildOrder> getSortedOpenChildOrdersBySymbol(String symbol) {
		ArrayList<ChildOrder> list = new ArrayList<ChildOrder>();
		for(ChildOrder order: childOrders.values()) {
			if(order.getSymbol().equals(symbol))
				list.add(order);
		}
		return OrderUtils.getSortedOpenChildOrders(list);
	}
	
	public ChildOrder getChildOrder(String id) {
		return childOrders.get(id);
	}

	public ChildOrder removeChildOrder(String id) {
		return childOrders.remove(id);
	}

	public ChildOrder addChildOrder(ChildOrder order) {
		return childOrders.put(order.getId(), order);
	}
	
	public int childOrderCount() {
		return childOrders.size();
	}
	
	public void start() {
		if (hasPendingChildOrder()) {
			logWarn("Strategy can not be started since there is pending child order");
			return;
		}
		pendingExecInstrEvent = null;
		setExecFailCount(0);
		executionManager.init();
		logInfo("Strategy started");
		updateStrategyState(StrategyState.Running);
	}
	
	public void stop() {
		if(getState().equals(StrategyState.Terminated) ||
				getState().equals(StrategyState.Stopped) ||
				getState().equals(StrategyState.Stopping))
			return;
		logInfo("Strategy stopping");
		updateStrategyState(StrategyState.Stopping);
	}
	
	public void pause() {
		if(getState().equals(StrategyState.Terminated))
			return;
		logInfo("Strategy paused");
		updateStrategyState(StrategyState.Paused);
	}

	public void terminate() {
		uninit();
		container.sendEvent(new RemoveStrategyEvent(null, getId()));
		logInfo("Strategy terminated");
		updateStrategyState(StrategyState.Terminated);
	}

	protected void updateExecuteTiming(ExecuteTiming timing) {
		if(timing.compareTo(getExecuteTiming()) > 0)
			setExecuteTiming(timing);
	}
	
	protected void timeOutCheck() {
		if (getState().equals(StrategyState.Running) 
				&& TimeUtil.getTimePass(pendingExecInstrEvent.getTime()) > getExecPendingTimeOut()) {
				if(getState().equals(StrategyState.Running)) {
					String info = "Pause strategy because execution instruction pending since " + pendingExecInstrEvent;
					info += " now is " + Clock.getInstance().now();
					error(info);
				}
		}
	}

	protected void processExecuteEvent(ExecuteEvent event) {
		execute(event.getTiming());
	}
	
	protected void executeWithTiming(ExecuteTiming defaultTiming, Class<? extends AsyncEvent> eventClass) {
		ExecuteTiming timing;
		if(null != eventExecuteTiming && null != (timing = eventExecuteTiming.get(eventClass))) {
			log.debug("Executing with configured timing: " + eventClass + " : " + timing);
			execute(timing);
		} else {
			execute(defaultTiming);
		}
	}

	protected void processStrategyEndTimerEvent(StrategyEndTimerEvent event) {
		logDebug("Strategy end timer fired");
		execute(ExecuteTiming.ASAP);
	}

	protected void processStrategyStartTimerEvent(StrategyStartTimerEvent event) {
		logDebug("Strategy start timer fired");
		execute(ExecuteTiming.ASAP);
	}


	protected boolean checkStartEndTime() {
		Date start = getStartTime();
		Date end = getEndTime();
		Date now = Clock.getInstance().now();
		if(null != end && !end.after(now)) {
			logInfo("Hit strategy end time, stopping strategy");
			stop();
			return false; // let it fall through so execute will do stopping handling
		}
		
		if(null != start && start.after(now)) {
			withDrawAllChildOrders();
			return true; // no further actions
		}
		
		return false;
	}
	
	protected void withDrawAllChildOrders() {
		if(childOrderCount() == 0)
			return;
		
		PriceInstruction pi = new PriceInstruction();
		List<ExecutionInstruction> ei = executionAnalyzer.analyze(pi, this);
		logInfo("ExecutionInstruction: " + ei);
		executeInstructions(ei);
	}
	
	protected void executeInstructions(List<ExecutionInstruction> eis) {
		if(eis != null && eis.size()>0) {
			setLastActionTime(Clock.getInstance().now());
			ExecutionInstructionEvent event = new ExecutionInstructionEvent(getId(), eis);
			if(pendingAckHandler != null)
				pendingAckHandler.setPendingInstructionEventId(event.getId());
			pendingExecInstrEvent = event;
			container.sendEvent(event);
		} else if (pendingAckHandler != null) { // no action needed for down stream so pendingAck can be ack now
			logDebug("Ack pending since no risk-avoid changes are needed");
			pendingAckHandler.postHandle(true, null);
			pendingAckHandler = null;
		}
	}
	
	protected boolean preCheck() {
		if(checkCompletion())
			return false;

		if(getExecuteTiming().equals(ExecuteTiming.NOW))
			return true;
		
		if(getExecuteTiming().equals(ExecuteTiming.ASAP) && TimeUtil.getTimePass(getLastExecuted()) > getHpInterval())
			return true;
		
		if(TimeUtil.getTimePass(getLastExecuted()) > getLpInterval())
			return true;
		
		return false;
	}
	
	protected void processAsyncTimerEvent(AsyncTimerEvent event) {
		executionManager.onMaintenance();
		executeWithTiming(ExecuteTiming.NORMAL, event.getClass());
	}
	
	protected void processExecutionInstructionResponseEvent(ExecutionInstructionResponseEvent event) {
		logDebug("Transaction completed: " + event.getId() + ", " + event.isSuccess());
		if(event.isSuccess()) {
			setExecFailCount(0); 
		} else {
			if(getState().equals(StrategyState.Running)) {
				incExecFailCount();
				if (getExecFailCount() > getExecFailLimit()) {
					String info = "Execution has failed more than " + getExecFailLimit() + " times, strategy paused";
					log.error(info);
					error(info); // included pendingAckHandling
				}
			}
		}
		//handle ack, nack is handled above in error()
		if(null != pendingAckHandler && event.getId().equals(pendingAckHandler.getPendingInstructionEventId())) {
			pendingAckHandler.postHandle(event.isSuccess(), event.getInfo());
			pendingAckHandler = null;
		}
		pendingExecInstrEvent = null;
	}

	protected void processChildOrderUpdateEvent(ChildOrderUpdateEvent event) {
		logDebug("Received child order update: " + event.getExecType() + " - " + event.getOrder() 
				+ ", message: " + event.getMessage());

		ChildOrder child = event.getOrder();
		if(PriceUtils.EqualGreaterThan(child.getCumQty(), child.getQuantity()) || 
				child.getOrdStatus().isCompleted()) {
			removeChildOrder(child.getId());
		} else if (child.getOrdStatus().equals(OrdStatus.NEW) || 
				child.getOrdStatus().equals(OrdStatus.PENDING_NEW)){
			addChildOrder(child);
		} else {
			ChildOrder prev = addChildOrder(child);
			if(prev == null) {
				log.error("Received child order update but cant find in active orders: " + child);
			}
		}
	}
	
	protected boolean checkPendingAck(String id, String sender) {
		if(null != pendingAckHandler) {
			container.sendRemoteEvent(
					new ManualActionReplyEvent(id, sender, false, "Parent order is pending on action"));
			return false;
		}
		return true;
	}
	
	protected void processMarketSessionEvent(MarketSessionEvent event) {
		logDebug("Market session has changed to: " + event.getSession());
		this.marketSession = event.getSession();
		executeWithTiming(ExecuteTiming.ASAP, event.getClass());
	}

	protected boolean checkCompletion() {
		return false;
	}
	
	protected void error(String info) {
		if(getState().equals(StrategyState.Running)) {
			updateStrategyState(StrategyState.Error);
			logError("Strategy has error and pasued: " + info);
		}
		if(null != pendingAckHandler) {
			pendingAckHandler.postHandle(false, info);
			pendingAckHandler = null;
		}
	}

	public abstract ChildOrder createChildOrder(String parentId, String symbol, OrderSide side, 
			double quantity, double price, ExchangeOrderType type);
	public void init() throws StrategyException {
		if(executionManager == null) {
			throw new StrategyException("executionManager is not set");
		}
		executionManager.setStrategy(this);
		executionManager.setSender(sender);
		executionManager.init();
	}
	public abstract void uninit();
	public abstract void execute(ExecuteTiming timing);
	public abstract Date getStartTime();
	public abstract Date getEndTime();
	protected abstract void updateStrategyState(StrategyState state);
	public abstract String getId();
	// for strategy factory to call
	public abstract void create(Object... objects) throws StrategyException;
	
	///////////////////////
	//
	// getters and setters
	//
	///////////////////////


	public void setContainer(IStrategyContainer container) {
		this.container = container;
	}

	public IStrategyContainer getContainer() {
		return container;
	}
	
	@Override
	public IDownStreamSender getSender() {
		return sender;
	}

	public void setSender(IDownStreamSender sender) {
		this.sender = sender;
	}

	@Override
	public IAsyncEventInbox getInbox() {
		return container.getInbox();
	}

	public Date getLastActionTime() {
		return lastActionTime;
	}

	public long getExecPendingTimeOut() {
		return execPendingTimeOut;
	}

	public void setExecPendingTimeOut(long execPendingTimeOut) {
		this.execPendingTimeOut = execPendingTimeOut;
	}

	public int getExecFailLimit() {
		return execFailLimit;
	}

	public void setExecFailLimit(int execFailLimit) {
		this.execFailLimit = execFailLimit;
	}

	public long getLpInterval() {
		return lpInterval;
	}

	public void setLpInterval(long lpInterval) {
		this.lpInterval = lpInterval;
	}

	public long getHpInterval() {
		return hpInterval;
	}

	public void setHpInterval(long hpInterval) {
		this.hpInterval = hpInterval;
	}

	public int getTimerInterval() {
		return timerInterval;
	}

	public void setTimerInterval(int timerInterval) {
		this.timerInterval = timerInterval;
	}

	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}

	public int getExecFailCount() {
		return execFailCount;
	}

	public void setExecFailCount(int execFailCount) {
		this.execFailCount = execFailCount;
	}
	
	public void incExecFailCount() {
		this.execFailCount++;
	}

	public ExecuteTiming getExecuteTiming() {
		return executeTiming;
	}

	public void setExecuteTiming(ExecuteTiming executeTiming) {
		this.executeTiming = executeTiming;
	}

	public Date getLastExecuted() {
		return lastExecuted;
	}

	public void setLastExecuted(Date lastExecuted) {
		this.lastExecuted = lastExecuted;
	}

	public void setLastActionTime(Date lastActionTime) {
		this.lastActionTime = lastActionTime;
	}

	public void setLastExecutionTime(Date lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

	public IExecutionAnalyzer getExecutionAnalyzer() {
		return executionAnalyzer;
	}


	public void setExecutionAnalyzer(IExecutionAnalyzer executionAnalyer) {
		this.executionAnalyzer = executionAnalyer;
	}


	public IExecutionManager getExecutionManager() {
		return executionManager;
	}


	public void setExecutionManager(IExecutionManager executionManager) {
		this.executionManager = executionManager;
	}

	@Override
	public boolean isCheckAdjQuote() {
		return checkAdjQuote;
	}

	@Override
	public void setCheckAdjQuote(boolean checkAdjQuote) {
		this.checkAdjQuote = checkAdjQuote;
	}

	@Override
	public boolean isValidateQuote() {
		return validateQuote;
	}

	@Override
	public void setValidateQuote(boolean validateQuote) {
		this.validateQuote = validateQuote;
	}

	public Map<Class<? extends AsyncEvent>, ExecuteTiming> getEventExecuteTiming() {
		return eventExecuteTiming;
	}

	public void setEventExecuteTiming(
			Map<Class<? extends AsyncEvent>, ExecuteTiming> eventExecuteTiming) {
		this.eventExecuteTiming = eventExecuteTiming;
	}

	public String getStrategyName() {
		return this.strategyName;
	}

	public void setStrategyName(String strategyName) {
		this.strategyName = strategyName;
	}

	public void setStrategyFieldDefs(List<FieldDef> strategyFieldDefs) {
		this.strategyFieldDefs = strategyFieldDefs;
	}

	public List<FieldDef> getStrategyFieldDefs() {
		return strategyFieldDefs;
	}

}
