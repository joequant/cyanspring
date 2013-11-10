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
package com.cyanspring.sample.singleorder.pov;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.singleorder.AbstractPriceAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class POVPriceAnalyzer extends AbstractPriceAnalyzer {

	@Override
	protected PriceInstruction calculate(QuantityInstruction qtyInstruction,
			SingleOrderStrategy strategy) {
		ParentOrder order = strategy.getParentOrder();
		PriceInstruction pi = new PriceInstruction();
		ITickTable tickTable = strategy.getTickTable();
		
		if(PriceUtils.GreaterThan(qtyInstruction.getAggresiveQty(), 0)) {
			double price;
			if(order.getSide().equals(OrderSide.Buy))
				price = strategy.getAdjQuote().getAsk();
			else
				price = strategy.getAdjQuote().getBid();
			
			if(tickTable.validPrice(price))
				pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, qtyInstruction.getAggresiveQty(), 
						ExchangeOrderType.LIMIT, strategy.getId()));
		}
		
//		if(PriceUtils.GreaterThan(qtyInstruction.getPassiveQty(), 0)) {
			double price;
			if(order.getSide().equals(OrderSide.Buy))
				price = strategy.getAdjQuote().getBid();
			else
				price = strategy.getAdjQuote().getAsk();
			
			if(tickTable.validPrice(price))
				pi.add(new PriceAllocation(order.getSymbol(), order.getSide(), price, qtyInstruction.getPassiveQty(), 
						ExchangeOrderType.LIMIT, strategy.getId()));
//		}

		return pi;
	}

}
