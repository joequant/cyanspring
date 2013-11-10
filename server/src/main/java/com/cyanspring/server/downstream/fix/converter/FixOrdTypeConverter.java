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
package com.cyanspring.server.downstream.fix.converter;

import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.IDataConverter;
import com.cyanspring.common.type.OrderType;

public class FixOrdTypeConverter implements IDataConverter {

	@Override
	public Object fromString(String value) throws DataConvertException {
		if (value.equals("1"))
			return OrderType.Market;
		
		if (value.equals("2"))
			return OrderType.Limit;
		
		throw new DataConvertException("Fix OrdType not supported: " + value);
	}

	@Override
	public String toString(Object object) throws DataConvertException {
		OrderType type = (OrderType)object;
		if(type.equals(OrderType.Market))
			return "1";
		
		if(type.equals(OrderType.Limit))
			return "2";
		
		throw new DataConvertException("Can't convert to FIX OrdType: " + object);
	}

}
