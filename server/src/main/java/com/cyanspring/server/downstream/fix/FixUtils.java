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
package com.cyanspring.server.downstream.fix;

import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;

public class FixUtils {
	public static char toFixOrderSide(OrderSide side) throws FixConvertException
	{
		if (side == OrderSide.Buy)
			return '1';
		else if (side == OrderSide.Sell)
			return '2';
		else if (side == OrderSide.SS)
			return '5';
		else
			throw new FixConvertException("toFixOrderSide: cant map side " + side);
	}	
	
	public static OrderSide fromFixOrderSide(char side) throws FixConvertException
	{
    	if (side == '1')
    		return OrderSide.Buy;
    	else if (side == '2')
    		return OrderSide.Sell;
		else if (side == '5')
			return OrderSide.SS;
    	else
			throw new FixConvertException("fromFixOrderSide: cant map side " + side);
	}	
	
	public static char toFixExchangeOrderType(ExchangeOrderType type) throws FixConvertException
	{
		if (type == ExchangeOrderType.MARKET)
			return '1';
		else
			return '2';
		// add more handling here if you want to support more exchange order type
	}	
	
	public static ExchangeOrderType fromFixExchangeOrderType(char type) throws FixConvertException
	{
		if (type == '1')
			return ExchangeOrderType.MARKET;
		else 
			return ExchangeOrderType.LIMIT;
		// add more handling here if you want to support more exchange order type
	}	
	
	public static char toFixOrderType(OrderType type) throws FixConvertException
	{
		if (type == OrderType.Market)
			return '1';
		else
			return '2';
		// add more handling here if you want to support more exchange order type
	}	
	
	public static OrderType fromFixOrderType(char type) throws FixConvertException
	{
		if (type == '1')
			return OrderType.Market;
		else 
			return OrderType.Limit;
		// add more handling here if you want to support more exchange order type
	}	
}
