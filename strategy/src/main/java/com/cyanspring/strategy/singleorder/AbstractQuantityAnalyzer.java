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

import com.cyanspring.common.staticdata.RefData;

public abstract class AbstractQuantityAnalyzer implements IQuantityAnalyzer {
	protected void finalizeQty(QuantityInstruction qi, SingleOrderStrategy strategy) {
		if(null == qi)
			return;
		
		RefData refData = strategy.getRefData();
		// round to remove negative qty if any from previous calculation
		qi.setAggresiveQty(refData.roundToLots(qi.getAggresiveQty()));
		qi.setPassiveQty(refData.roundToLots(qi.getPassiveQty()));

		double remainingQty = strategy.getParentOrder().getRemainingQty();
		if(qi.getAggresiveQty() > remainingQty) {
			qi.setAggresiveQty(remainingQty);
			qi.setPassiveQty(0);
			strategy.logDebug("Capping aggressive quantity to: " + qi.getAggresiveQty());
		} else if (qi.getAggresiveQty() + qi.getPassiveQty() > remainingQty) {
			qi.setPassiveQty(remainingQty - qi.getAggresiveQty());
			strategy.logDebug("Capping passive quantity to: " + qi.getPassiveQty());
		}
		
		// round again incase any odd lots left from calculation
		double aggressiveQty = qi.getAggresiveQty();
		qi.setAggresiveQty(refData.roundToLots(qi.getAggresiveQty()));
		strategy.logDebug("Round aggressive qty from " + aggressiveQty + " to " + qi.getAggresiveQty());
		
		double passiveQty = qi.getPassiveQty();
		qi.setPassiveQty(refData.roundToLots(qi.getPassiveQty()));
		strategy.logDebug("Round passive qty from " + passiveQty + " to " + qi.getPassiveQty());
	}
	
	abstract protected QuantityInstruction calculate(SingleOrderStrategy strategy);
	
	@Override
	public QuantityInstruction analyze(SingleOrderStrategy strategy) {
		QuantityInstruction qi = calculate(strategy);
		finalizeQty(qi, strategy);
		return qi;
	}

}
