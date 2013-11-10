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
import org.springframework.beans.factory.annotation.Autowired;
import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.strategy.IStrategyFactory;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.validation.OrderValidationException;

public class ParentOrderPreCheck extends AbstractOrderValidator {
	@Autowired
	IStrategyFactory strategyFactory;
	
	@Override
	protected void myValidation(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException, StrategyException {
		// check fields are presented for basic order parameters
		fieldEmptyCheck(map, OrderField.SYMBOL.value());
		fieldEmptyCheck(map, OrderField.TYPE.value());
		Object objOrderType = map.get(OrderField.TYPE.value());
		boolean mktType = (objOrderType instanceof OrderType && ((OrderType)objOrderType) == OrderType.Market) ||
						  (objOrderType instanceof String && ((String)objOrderType).equals(OrderType.Market.toString()));
		if(!mktType)
			fieldEmptyCheck(map, OrderField.PRICE.value());
		fieldEmptyCheck(map, OrderField.QUANTITY.value());
		fieldEmptyCheck(map, OrderField.SIDE.value());
		fieldEmptyCheck(map, OrderField.STRATEGY.value());
		
		// check compulsory parameters are presented for strategy
		String strategy = (String)map.get(OrderField.STRATEGY.value());
		Map<String, FieldDef> fieldDefs = strategyFactory.getStrategyFieldDef(strategy);

		if(null == fieldDefs)
			throw new OrderValidationException("Strategy is not defined in strategyFields map: " + strategy);
			
		for(FieldDef fieldDef: fieldDefs.values()) {
			if (fieldDef.isInput()) {
				if (!map.containsKey(fieldDef.getName()))
					throw new OrderValidationException("Required parameter [" + fieldDef.getName() + 
							"] is missing for this strategy: " + strategy);
				fieldEmptyCheck(map, fieldDef.getName());
			}
		}
		
	}
}
