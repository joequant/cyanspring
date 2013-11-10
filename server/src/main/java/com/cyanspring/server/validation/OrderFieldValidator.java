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

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class OrderFieldValidator extends AbstractOrderValidator implements
		IOrderValidator {

	private Map<String, IFieldValidator> fieldValidators;
	
	@Override
	protected void myValidation(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		if(fieldValidators == null)
			return;
		
		for(Entry<String, Object> entry: map.entrySet()) {
			IFieldValidator fieldValidator = fieldValidators.get(entry.getKey());
			if(null == fieldValidator)
				continue;
			
			fieldValidator.validate(entry.getKey(), entry.getValue(), map, order);
		}
	}

	public Map<String, IFieldValidator> getFieldValidators() {
		return fieldValidators;
	}

	public void setFieldValidators(Map<String, IFieldValidator> fieldValidators) {
		this.fieldValidators = fieldValidators;
	}

}
