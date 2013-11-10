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
package com.cyanspring.common.strategy;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;

public class PriceAllocation {
	String symbol;
	OrderSide side;
	double price;
	double qty;
	ExchangeOrderType orderType;
	String parentId;

	public PriceAllocation(String symbol, OrderSide side, double price,
			double qty, ExchangeOrderType orderType, String parentId) {
		super();
		this.symbol = symbol;
		this.side = side;
		this.price = price;
		this.qty = qty;
		this.orderType = orderType;
		this.parentId = parentId;
	}


	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getSymbol() {
		return symbol;
	}

	public OrderSide getSide() {
		return side;
	}

	public double getPrice() {
		return price;
	}

	public double getQty() {
		return qty;
	}

	public ExchangeOrderType getOrderType() {
		return orderType;
	}

	public void setQty(double qty) {
		this.qty = qty;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public boolean matches(ChildOrder order) {
		return 	this.getParentId().equals(order.getParentOrderId()) &&
				this.getSymbol().equals(order.getSymbol()) &&
				this.getSide().equals(order.getSide()) &&
				this.getOrderType().equals(order.getType()) &&
				PriceUtils.Equal(this.getPrice(), order.getPrice());
	}
	
	@Override
	public String toString() {
		return "[" + symbol + ", " +
		side.toString() + ", " +
		price + ", " +
		qty + ", " +
		orderType + "]";
	}
}
