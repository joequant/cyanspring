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
package com.cyanspring.sample.singleorder.sdma;

import com.cyanspring.strategy.singleorder.AbstractQuantityAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;


public class SDMAQuantityAnalyzer extends AbstractQuantityAnalyzer {
	
	@Override
	public QuantityInstruction calculate(SingleOrderStrategy strategy) {
		QuantityInstruction instruction = new QuantityInstruction();
		instruction.setAggresiveQty(strategy.getParentOrder().getRemainingQty());
		return instruction;
	}

}
