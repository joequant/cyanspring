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

import java.util.List;
import java.util.Map;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public abstract class AbstractOrderValidator implements IOrderValidator {
	private List<IOrderValidator> validators;
	
	public AbstractOrderValidator() {
		super();
	}
	
	public AbstractOrderValidator(List<IOrderValidator> validators) {
		super();
		this.validators = validators;
	}

	protected void fieldEmptyCheck(Map<String, Object> map, String field) throws OrderValidationException {
		Object obj = map.get(field);
		if(null == obj || (obj instanceof String && ((String)obj).equals(""))) {
			throw new OrderValidationException("Required field [" + field + "] is empty");
		}
	}
	
	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		
		try {
			myValidation(map, order);
			if (validators != null) {
				for(IOrderValidator validator: validators) {
					validator.validate(map, order);
				}
			}
		} catch (OrderValidationException oe) {
			throw oe;
		} catch (Exception e) {
			e.printStackTrace();
			throw new OrderValidationException(e.getMessage());
		}
	}
	
	abstract protected void myValidation(Map<String, Object> map, ParentOrder order) throws OrderValidationException, StrategyException;
}
