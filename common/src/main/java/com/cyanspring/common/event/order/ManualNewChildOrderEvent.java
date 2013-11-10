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
package com.cyanspring.common.event.order;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;

public class ManualNewChildOrderEvent extends RemoteAsyncEvent {
	String parentId;
	OrderSide side;
	double price;
	double quantity;
	ExchangeOrderType orderType;
	
	public ManualNewChildOrderEvent(String key, String receiver,
			String parentId, OrderSide side, double price, double quantity,
			ExchangeOrderType orderType) {
		super(key, receiver);
		this.parentId = parentId;
		this.side = side;
		this.price = price;
		this.quantity = quantity;
		this.orderType = orderType;
	}

	public double getPrice() {
		return price;
	}
	public double getQuantity() {
		return quantity;
	}
	public ExchangeOrderType getOrderType() {
		return orderType;
	}
	public String getParentId() {
		return parentId;
	}
	public OrderSide getSide() {
		return side;
	}
	
}
