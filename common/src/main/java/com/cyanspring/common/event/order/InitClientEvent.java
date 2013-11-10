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
package com.cyanspring.common.event.order;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.MultiInstrumentStrategyDisplayConfig;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketsession.DefaultStartEndTime;

public class InitClientEvent extends RemoteAsyncEvent {

	private List<String> singleOrderDisplayFields;
	private Map<String, Map<String, FieldDef>> singleOrderFieldDefs;
	private List<String> singleInstrumentDisplayFields;
	private Map<String, Map<String, FieldDef>> singleInstrumentFieldDefs;
	private List<String> multiInstrumentDisplayFields;
	private Map<String, MultiInstrumentStrategyDisplayConfig> multiInstrumentStrategyFieldDefs;
	private DefaultStartEndTime defaultStartEndTime;
	
	public InitClientEvent(
			String key,
			String receiver,
			List<String> singleOrderDisplayFields,
			Map<String, Map<String, FieldDef>> singleOrderFieldDefs,
			List<String> singleInstrumentDisplayFields,
			Map<String, Map<String, FieldDef>> singleInstrumentFieldDefs,
			List<String> multiInstrumentDisplayFields,
			Map<String, MultiInstrumentStrategyDisplayConfig> multiInstrumentStrategyFieldDefs,
			DefaultStartEndTime defaultStartEndTime) {
		super(key, receiver);
		this.singleOrderDisplayFields = singleOrderDisplayFields;
		this.singleOrderFieldDefs = singleOrderFieldDefs;
		this.singleInstrumentDisplayFields = singleInstrumentDisplayFields;
		this.singleInstrumentFieldDefs = singleInstrumentFieldDefs;
		this.multiInstrumentDisplayFields = multiInstrumentDisplayFields;
		this.multiInstrumentStrategyFieldDefs = multiInstrumentStrategyFieldDefs;
		this.defaultStartEndTime = defaultStartEndTime;
	}

	public DefaultStartEndTime getDefaultStartEndTime() {
		return defaultStartEndTime;
	}

	public List<String> getSingleOrderDisplayFields() {
		return singleOrderDisplayFields;
	}

	public void setSingleOrderDisplayFields(
			List<String> singleOrderDisplayFields) {
		this.singleOrderDisplayFields = singleOrderDisplayFields;
	}

	public List<String> getMultiInstrumentDisplayFields() {
		return multiInstrumentDisplayFields;
	}

	public void setMultiInstrumentDisplayFields(
			List<String> multiInstrumentDisplayFields) {
		this.multiInstrumentDisplayFields = multiInstrumentDisplayFields;
	}

	public Map<String, MultiInstrumentStrategyDisplayConfig> getMultiInstrumentStrategyFieldDefs() {
		return multiInstrumentStrategyFieldDefs;
	}

	public void setMultiInstrumentStrategyFieldDefs(
			Map<String, MultiInstrumentStrategyDisplayConfig> multiInstrumentStrategyFieldDefs) {
		this.multiInstrumentStrategyFieldDefs = multiInstrumentStrategyFieldDefs;
	}

	public void setDefaultStartEndTime(DefaultStartEndTime defaultStartEndTime) {
		this.defaultStartEndTime = defaultStartEndTime;
	}

	public Map<String, Map<String, FieldDef>> getSingleOrderFieldDefs() {
		return singleOrderFieldDefs;
	}

	public void setSingleOrderFieldDefs(
			Map<String, Map<String, FieldDef>> singleOrderFieldDefs) {
		this.singleOrderFieldDefs = singleOrderFieldDefs;
	}

	public List<String> getSingleInstrumentDisplayFields() {
		return singleInstrumentDisplayFields;
	}

	public void setSingleInstrumentDisplayFields(
			List<String> singleInstrumentDisplayFields) {
		this.singleInstrumentDisplayFields = singleInstrumentDisplayFields;
	}

	public Map<String, Map<String, FieldDef>> getSingleInstrumentFieldDefs() {
		return singleInstrumentFieldDefs;
	}

	public void setSingleInstrumentFieldDefs(
			Map<String, Map<String, FieldDef>> singleInstrumentFieldDefs) {
		this.singleInstrumentFieldDefs = singleInstrumentFieldDefs;
	}

	
}
