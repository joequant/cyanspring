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
package com.cyanspring.common.business;

import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;

public class ChildOrder extends Order {
	@Override
	protected void init() {
		super.init();
		setOrdStatus(OrdStatus.PENDING_NEW);
	}

	public ChildOrder(String symbol, OrderSide side, double quantity,
			double price, ExchangeOrderType type, String parentOrderId, String strategyId) {
		super(symbol, side, quantity, price);
		put(OrderField.TYPE.value(), type);
		put(OrderField.PARENT_ORDER_ID.value(), parentOrderId);
		put(OrderField.STRATEGY_ID.value(), strategyId);
	}
	
	protected ChildOrder() {
		super();
	}

	@Override
	protected String generateId() {
		return IdGenerator.getInstance().getNextID() + "C";
	}

	public String getParentOrderId() {
		return get(String.class, OrderField.PARENT_ORDER_ID.value());
	}

	public ExchangeOrderType getType() {
		return get(ExchangeOrderType.class, OrderField.TYPE.value());
	}

	public String getStrategyId() {
		return get(String.class, OrderField.STRATEGY_ID.value());
	}
	
	public String getClOrderId() {
		return get(String.class, OrderField.CLORDERID.value());
	}
	
	public void setClOrderId(String clOrderId) {
		put(OrderField.CLORDERID.value(), clOrderId);
	}
	
	protected void setType(ExchangeOrderType type) {
		put(OrderField.TYPE.value(), type);
	}
	
	protected void setParentOrderId(String parentOrderId) {
		put(OrderField.PARENT_ORDER_ID.value(), parentOrderId);
	}
	
	protected void setStrategyId(String strategyId) {
		put(OrderField.STRATEGY_ID.value(), strategyId);
	}
	
	@Override
	protected String fieldsToString() {
		return super.fieldsToString() + ", " + getType() + ", " + getParentOrderId() + ", " + getStrategyId() + ", " + getClOrderId();
	}
	
	public ChildOrder clone() {
		return (ChildOrder)super.clone();
	}

}
