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
import com.cyanspring.common.type.OrderSide;

public class FixSideConverter implements IDataConverter {

	@Override
	public Object fromString(String value) throws DataConvertException {
    	if (value.equals("1"))
    		return OrderSide.Buy;
    	
    	if (value.equals("2"))
    		return OrderSide.Sell;
		
    	if (value.equals("5"))
			return OrderSide.SS;

		throw new DataConvertException("Fix side not handdled: " + value);
	}

	@Override
	public String toString(Object object) throws DataConvertException {
		OrderSide side = (OrderSide)object;
		if(side.equals(OrderSide.Buy))
			return "1";
		
		if(side.equals(OrderSide.Sell))
			return "2";
		
		if(side.equals(OrderSide.SS))
			return "3";
		
		throw new DataConvertException("Can't convert value to FIX side: " + object);
	}

}
