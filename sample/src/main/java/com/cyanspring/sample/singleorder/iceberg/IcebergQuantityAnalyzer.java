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
package com.cyanspring.sample.singleorder.iceberg;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.strategy.singleorder.AbstractQuantityAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;

public class IcebergQuantityAnalyzer extends AbstractQuantityAnalyzer {

	@Override
	protected QuantityInstruction calculate(SingleOrderStrategy strategy) {
		ParentOrder order = strategy.getParentOrder();
		Double disQty = order.get(Double.class, OrderField.DISPLAY_QUANTITY.value());
		if(null == disQty)
			strategy.logError("Missing " + OrderField.DISPLAY_QUANTITY.value() + " Parameter");
		
		QuantityInstruction qi = new QuantityInstruction();
		qi.setPassiveQty(disQty);
		return qi;
	}

}
