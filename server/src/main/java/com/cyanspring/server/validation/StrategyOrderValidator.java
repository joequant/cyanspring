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

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class StrategyOrderValidator extends AbstractOrderValidator implements IOrderValidator {
	private Map<String, IOrderValidator> strategyValidators;
	

	@Override
	protected void myValidation(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		String strategy = (String) map.get(OrderField.STRATEGY.value());
		if(null == strategy)
			return;
		
		IOrderValidator validator = strategyValidators.get(strategy);
		if(null == validator)
			return;
		validator.validate(map, order);
	}


	public Map<String, IOrderValidator> getStrategyValidators() {
		return strategyValidators;
	}


	public void setStrategyValidators(
			Map<String, IOrderValidator> strategyValidators) {
		this.strategyValidators = strategyValidators;
	}

}
