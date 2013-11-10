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

import webcurve.util.PriceUtils;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.validation.IOrderValidator;
import com.cyanspring.common.validation.OrderValidationException;

public class SniperValidator implements IOrderValidator {

	@Override
	public void validate(Map<String, Object> map, ParentOrder order)
			throws OrderValidationException {
		Object op = map.get(OrderField.PRICE.value());
		if( (op == null) && map.containsKey(OrderField.PRICE.value()) || // price is specified with no value
			(op == null && order == null)) // new order contains no price
			throw new OrderValidationException("price can not be empty for Sniper stratgy");
			
		if(op !=  null && ((op instanceof String) && op.equals("")))
			throw new OrderValidationException("price can not be empty for Sniper stratgy");
		
		if(op !=  null && op instanceof Double && PriceUtils.EqualLessThan((Double)op, 0)) {
			throw new OrderValidationException("price can not be 0 for Sniper stratgy");
		}
	}

}
