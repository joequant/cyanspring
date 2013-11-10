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
package com.cyanspring.server.validation;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.strategy.IStrategyFactory;

public class ParentOrderDefaultValueFiller {
	private static final Logger log = LoggerFactory
			.getLogger(ParentOrderDefaultValueFiller.class);
	@Autowired
	private GenericDataConverter dataConverter;

	@Autowired
	IStrategyFactory strategyFactory;

	Map<Map<String, String>, Map<String, String>> defaults; 
	public ParentOrderDefaultValueFiller(Map<Map<String, String>, Map<String, String>> defaults) {
		this.defaults = defaults;
	}
	
	public void fillDefaults(Map<String, Object> fields) throws StrategyException {
		for(Entry<Map<String, String>, Map<String, String>> condValues: defaults.entrySet()) {
			Map<String, String> conditions = condValues.getKey();
			
			//check whether condictions met
			boolean conditionsMet = true;
			for(Entry<String, String> con: conditions.entrySet()) {
				Object value = fields.get(con.getKey());
				if(null == value) {
					conditionsMet = false;
					break;
				}
				if(value instanceof String) {
					if(!con.getValue().equals(value)) {
						conditionsMet = false;
						break;
					}
				} else {
					String strategyName = (String) fields.get(OrderField.STRATEGY.value());
					if(null == strategyName) {
						conditionsMet = false;
						break;
					}
					Map<String, FieldDef> fieldDefs = strategyFactory.getStrategyFieldDef(strategyName);
					FieldDef fieldDef = fieldDefs.get(con.getKey());
					if(fieldDef == null) { //not found in input fields
						conditionsMet = false;
						log.warn("Field not defined in input fields: " + con.getKey());
						break;
					}
					try {
						Object obj = dataConverter.fromString(fieldDef.getType(), con.getKey(), (String)con.getValue());
						if(!obj.equals(value)) {
							conditionsMet = false;
							break;
						}
					} catch (DataConvertException e) {
						log.error(e.getMessage(), e);
						e.printStackTrace();
						conditionsMet = false;
						break;
					}
				}
			}
			
			// if conditions met, fill the values specified
			if(conditionsMet) {
				for(Entry<String, String> fill: condValues.getValue().entrySet()) {
					//if the field already has a value, do not overwrite
					Object existingValue = fields.get(fill.getKey());
					if(null == existingValue) {
						fields.put(fill.getKey(), fill.getValue());
					}
				}
			}
		}
	}
}
