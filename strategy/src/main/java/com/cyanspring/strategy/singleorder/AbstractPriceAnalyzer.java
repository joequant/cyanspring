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
package com.cyanspring.strategy.singleorder;

import java.util.ArrayList;
import java.util.TreeSet;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.staticdata.ITickTable;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.util.PriceUtils;

public abstract class AbstractPriceAnalyzer implements IPriceAnalyzer {

	protected abstract PriceInstruction calculate(QuantityInstruction qtyInstruction,
			SingleOrderStrategy strategy);
	
	protected void finalizePrice(PriceInstruction pi,
			SingleOrderStrategy strategy) {
		if(null == pi)
			return;
		
		ParentOrder order = strategy.getParentOrder();
		ITickTable tickTable = strategy.getTickTable();
		TreeSet<PriceAllocation> pas = pi.getAllocationsById(strategy.getId());
		
		if(null == pas)
			return;
		
		// remove any 0 price instructions
		ArrayList<PriceAllocation> toBeRemoved = new ArrayList<PriceAllocation>();
		for(PriceAllocation pa: pas) {
			if(PriceUtils.Equal(pa.getPrice(), 0))
				toBeRemoved.add(pa);
		}
		for(PriceAllocation pa: toBeRemoved) {
			pas.remove(pa);
			strategy.logDebug("Remove 0 price allocation: " + pa);
		}
		
		// restrict to within price limit price
		for(PriceAllocation pa: pas) {
			if(!order.getOrderType().equals(OrderType.Market) &&
			   !PriceUtils.Equal(order.getPrice(), 0) && 
			   ((pa.getSide().isBuy() && PriceUtils.GreaterThan(pa.getPrice(), order.getPrice())) ||
				(pa.getSide().isSell() && PriceUtils.LessThan(pa.getPrice(), order.getPrice())))) {
					strategy.logDebug("Restrict order to price limit: " + order.getPrice());
					pa.setPrice(order.getPrice());
			}

			// round to ticks
			pa.setPrice(tickTable.getRoundedPrice(pa.getPrice(), order.getSide().isSell()));
		}
		
		// there are chances that more than one entries have the same price after adjusted so
		// go through the list to merge them again
		ArrayList<PriceAllocation> tmp = new ArrayList<PriceAllocation>();
		tmp.addAll(pas);
		pi.clear();
		for(PriceAllocation pa: tmp) {
			pi.add(pa);
		}
	}
	
	@Override
	public PriceInstruction analyze(QuantityInstruction qtyInstruction,
			SingleOrderStrategy strategy) {
		if(null == qtyInstruction)
			return null;
		
//		if(PriceUtils.Equal(qtyInstruction.getAggresiveQty(), 0) &&
//				PriceUtils.Equal(qtyInstruction.getPassiveQty(), 0))
//			return null;
		PriceInstruction pi = calculate(qtyInstruction, strategy);
		finalizePrice(pi, strategy);
		return pi;
	}

}
