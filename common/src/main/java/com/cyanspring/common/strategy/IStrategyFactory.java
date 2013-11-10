package com.cyanspring.common.strategy;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.FieldDef;

public interface IStrategyFactory {
	void init() throws StrategyException;
	void uninit();
	public IStrategy createStrategy(String name, Object... objects) throws StrategyException;
	public boolean validStrategy(String name);
	List<IStrategy> getAllStrategyTemplates();
	public List<String> getStrategyAmendableFields(String strategy) throws StrategyException;
	Map<String, FieldDef> getStrategyFieldDef(String strategyName) throws StrategyException;
}
