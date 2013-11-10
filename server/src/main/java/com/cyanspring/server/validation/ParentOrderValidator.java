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
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class ParentOrderValidator extends AbstractOrderValidator implements IOrderValidator {
	
	public ParentOrderValidator(List<IOrderValidator> validators) {
		super(validators);
	}

	@Override
	protected void myValidation(Map<String, Object> map, ParentOrder order) throws OrderValidationException {
	}
	
}
