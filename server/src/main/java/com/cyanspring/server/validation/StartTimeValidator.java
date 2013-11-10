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

import java.util.Date;
import java.util.Map;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.validation.OrderValidationException;

public class StartTimeValidator implements IFieldValidator {

	@Override
	public void validate(String field, Object value, Map<String, Object> map,
			ParentOrder order) throws OrderValidationException {
		if(value == null)
			return;
		
		Date start = (Date)value;
		Date end = (Date)map.get(OrderField.END_TIME.value());
		if(null == end && null != order)
			end = order.getEndTime();
			
		if(null == end)
			return;
		
		if(end.equals(start ) || end.before(start))
			throw new OrderValidationException("end time " + end + " is the same or before start time " + start);
	}

}
