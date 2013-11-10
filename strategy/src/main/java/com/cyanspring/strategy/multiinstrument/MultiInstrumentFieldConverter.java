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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.strategy.StrategyException;

public class MultiInstrumentFieldConverter {
//	private List<String> strategyLevelCompulsoryFields;
//	private List<String> instrumentLevelCompulsoryFields;
//	@SuppressWarnings("rawtypes")
//	private Map<String, Class> typeMap;
	private GenericDataConverter dataConverter = new GenericDataConverter();
	
	public void checkCompulsoryStrategyFields(MultiInstrumentStrategy strategy, Map<String, Object> fields) throws StrategyException {
		Map<String, FieldDef> fieldDefs = strategy.getCombinedFieldDefs();
		for(FieldDef fieldDef: fieldDefs.values()) {
			if(fieldDef.isInput() && !fields.containsKey(fieldDef.getName()))
				throw new StrategyException("Compulsory strategy field " + fieldDef.getName() + " is missing");
		}
	}
	Map<String, Object> convertStrategyFields(MultiInstrumentStrategy strategy, Map<String, Object> fields) throws DataConvertException, StrategyException {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, FieldDef> fieldDefs = strategy.getCombinedFieldDefs();
		for(Entry<String, Object> entry: fields.entrySet()) {
			FieldDef fieldDef = fieldDefs.get(entry.getKey());
			if(null != fieldDef && 
			   !fieldDef.getType().equals(String.class) &&
			   entry.getValue().getClass().equals(String.class)) { // convert this field
				Object value = dataConverter.fromString(fieldDef.getType(), entry.getKey(), (String)(entry.getValue()));
				result.put(entry.getKey(), value);
			} else {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
	
	public void checkCompulsoryInstrumentFields(MultiInstrumentStrategy strategy, 
			List<Map<String, Object>> instruments) throws StrategyException {
		Map<String, FieldDef> fieldDefs = strategy.getCombinedInstrumentFieldDefs();
		for(Map<String, Object> map: instruments)
			for(FieldDef fieldDef: fieldDefs.values()) {
				if(fieldDef.isInput() && !map.containsKey(fieldDef.getName()))
					throw new StrategyException("Compulsory instrument field " + fieldDef.getName() + " is missing");
			}
	}
	
	List<Map<String, Object>> convertInstrumentFields(MultiInstrumentStrategy strategy, 
			List<Map<String, Object>> instruments) throws DataConvertException, StrategyException {
		Map<String, FieldDef> fieldDefs = strategy.getCombinedInstrumentFieldDefs();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> map: instruments) {
			Map<String, Object> instrument = new HashMap<String, Object>();
			for(Entry<String, Object> entry: map.entrySet()) {
				FieldDef fieldDef = fieldDefs.get(entry.getKey());
				if(null != fieldDef && 
				   !fieldDef.getType().equals(String.class) &&
				   entry.getValue().getClass().equals(String.class)) { // convert this field
					Object value = dataConverter.fromString(fieldDef.getType(), entry.getKey(), (String)(entry.getValue()));
					instrument.put(entry.getKey(), value);
				} else {
					instrument.put(entry.getKey(), entry.getValue());
				}
			}
			result.add(instrument);
		}
		return result;
	}

	public GenericDataConverter getDataConverter() {
		return dataConverter;
	}

	public void setDataConverter(GenericDataConverter converter) {
		this.dataConverter = converter;
	}
	
	
}
