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

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.validation.OrderValidationException;

public class PovValidator implements IFieldValidator {

	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {
		Double pov = (Double)value;
		
		if(pov == null)
			throw new OrderValidationException(field + "value is null");

		if (pov != null && (pov<=0 || pov > 100))
			throw new OrderValidationException(field + "out of range of (0, 100]");
		
	}

}
