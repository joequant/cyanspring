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
package com.cyanspring.common.business;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiInstrumentStrategyDisplayConfig {
	private String strategy;
	private Map<String, FieldDef> strategyLevelFieldDefs;
	private List<String> instrumentDisplayFields;
	private Map<String, FieldDef> instrumentLevelFieldDefs;
	public MultiInstrumentStrategyDisplayConfig(String strategy,
			Map<String, FieldDef> strategyLevelFieldDefs,
			List<String> instrumentDisplayFields,
			Map<String, FieldDef> instrumentLevelFieldDefs) {
		super();
		this.strategy = strategy;
		this.strategyLevelFieldDefs = strategyLevelFieldDefs;
		this.instrumentDisplayFields = instrumentDisplayFields;
		this.instrumentLevelFieldDefs = instrumentLevelFieldDefs;
	}
	public String getStrategy() {
		return strategy;
	}
	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}
	public Map<String, FieldDef> getStrategyLevelFieldDefs() {
		return strategyLevelFieldDefs;
	}
	public void setStrategyLevelFieldDefs(
			Map<String, FieldDef> strategyLevelFieldDefs) {
		this.strategyLevelFieldDefs = strategyLevelFieldDefs;
	}
	public List<String> getInstrumentDisplayFields() {
		return instrumentDisplayFields;
	}
	public void setInstrumentDisplayFields(List<String> instrumentDisplayFields) {
		this.instrumentDisplayFields = instrumentDisplayFields;
	}
	public Map<String, FieldDef> getInstrumentLevelFieldDefs() {
		return instrumentLevelFieldDefs;
	}
	public void setInstrumentLevelFieldDefs(
			Map<String, FieldDef> instrumentLevelFieldDefs) {
		this.instrumentLevelFieldDefs = instrumentLevelFieldDefs;
	}
	
	public List<String> getStrategyAmendable() {
		List<String> result = new ArrayList<String>();
		for(FieldDef fieldDef: strategyLevelFieldDefs.values()) {
			if(fieldDef.isAmendable())
				result.add(fieldDef.getName());
		}
		return result;
	}
	public List<String> getInstrumentAmendable() {
		List<String> result = new ArrayList<String>();
		for(FieldDef fieldDef: instrumentLevelFieldDefs.values()) {
			if(fieldDef.isAmendable())
				result.add(fieldDef.getName());
		}
		return result;
	}
	
}
