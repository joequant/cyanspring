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
package com.cyanspring.sample.singleorder.sniper;

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.strategy.singleorder.AbstractQuantityAnalyzer;
import com.cyanspring.strategy.singleorder.QuantityInstruction;
import com.cyanspring.strategy.singleorder.SingleOrderStrategy;
import com.cyanspring.strategy.utils.QuoteUtil;

public class SniperQuantityAnalyzer extends AbstractQuantityAnalyzer {

	@Override
	protected QuantityInstruction calculate(SingleOrderStrategy strategy) {
		Quote quote = strategy.getQuote();
		ParentOrder order = strategy.getParentOrder();
		double oppQty = QuoteUtil.getOppositeQuantityToPrice(quote, order.getPrice(), order.getSide());
		double qty = Math.min(oppQty, order.getRemainingQty());
		
		QuantityInstruction qi = new QuantityInstruction();
		qi.setAggresiveQty(qty);
		return qi;
	}

}
