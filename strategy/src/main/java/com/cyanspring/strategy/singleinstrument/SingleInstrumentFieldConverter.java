package com.cyanspring.strategy.singleinstrument;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.strategy.StrategyException;

public class SingleInstrumentFieldConverter {
	private GenericDataConverter dataConverter = new GenericDataConverter();
	
	public void checkCompulsoryInstrumentFields(SingleInstrumentStrategy strategy, 
			Map<String, Object> instrument) throws StrategyException {
		Map<String, FieldDef> fieldDefs = strategy.getCombinedFieldDefs();
		for(FieldDef fieldDef: fieldDefs.values()) {
			if(fieldDef.isInput() && !instrument.containsKey(fieldDef.getName()))
				throw new StrategyException("Compulsory instrument field " + fieldDef.getName() + " is missing");
		}
	}
	
	Map<String, Object> convertInstrumentFields(SingleInstrumentStrategy strategy, 
			Map<String, Object> instrument) throws DataConvertException, StrategyException {
		Map<String, FieldDef> fieldDefs = strategy.getCombinedFieldDefs();
		Map<String, Object> result = new HashMap<String, Object>();
		for(Entry<String, Object> entry: instrument.entrySet()) {
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

	public GenericDataConverter getDataConverter() {
		return dataConverter;
	}

	public void setDataConverter(GenericDataConverter converter) {
		this.dataConverter = converter;
	}
	

}
