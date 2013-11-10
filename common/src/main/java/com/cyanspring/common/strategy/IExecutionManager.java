package com.cyanspring.common.strategy;

import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.strategy.IStrategy;

public interface IExecutionManager {
	
	public IDownStreamSender getSender();
	public void setSender(IDownStreamSender sender);
	public IStrategy getStrategy();
	public void setStrategy(IStrategy strategy);
	public abstract boolean isPending();
	public abstract void init();
	public abstract void uninit();
	public void onMaintenance();
}
