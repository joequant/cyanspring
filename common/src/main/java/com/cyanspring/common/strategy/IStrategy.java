package com.cyanspring.common.strategy;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;

public interface IStrategy {
	public List<FieldDef> getCommonFieldDefs();
	public Map<String, FieldDef> getCombinedFieldDefs() throws StrategyException;
	
	public void logDebug(String message);
	public void logInfo(String message);
	public void logWarn(String message);
	public void logError(String message);
	
	public Collection<ChildOrder> getChildOrders();
	public List<ChildOrder> getOpenChildOrdersByParent(String parent);
	public Set<ChildOrder> getSortedOpenChildOrdersByParent(String parent);
	public Set<ChildOrder> getSortedOpenChildOrdersBySymbol(String symbol);
	
	public ChildOrder getChildOrder(String id);
	public ChildOrder removeChildOrder(String id);
	public ChildOrder addChildOrder(ChildOrder order);
	public int childOrderCount();
	
	public void start();
	public void stop();
	public void pause();
	public void terminate();

	public void init() throws StrategyException;
	public void uninit();
	public  void execute(ExecuteTiming timing);
	public  Date getStartTime();
	public  Date getEndTime();
	public  String getId();
	// for strategy factory to call
	public void create(Object... objects) throws StrategyException;
	public RemoteAsyncEvent createConfigUpdateEvent() throws StrategyException;
	public ChildOrder createChildOrder(String parentId, String symbol, OrderSide side, 
			double quantity, double price, ExchangeOrderType type);

	public void setContainer(IStrategyContainer container);
	public IStrategyContainer getContainer();
	public IDownStreamSender getSender();
	public IAsyncEventInbox getInbox();

	public IExecutionManager getExecutionManager();

	public boolean isCheckAdjQuote();
	public void setCheckAdjQuote(boolean checkAdjQuote);

	public String getStrategyName();

	public List<FieldDef> getStrategyFieldDefs();
	public void setSender(IDownStreamSender sender);
	boolean isValidateQuote();
	void setValidateQuote(boolean validateQuote);

}
