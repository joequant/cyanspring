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

public class ManualAmendChildOrderEvent extends RemoteAsyncEvent {
	String childOrderId;
	double price;
	double quantity;
	public ManualAmendChildOrderEvent(String key, String receiver,
			String childOrderId, double price, double quantity) {
		super(key, receiver);
		this.childOrderId = childOrderId;
		this.price = price;
		this.quantity = quantity;
	}
	public String getChildOrderId() {
		return childOrderId;
	}
	public double getPrice() {
		return price;
	}
	public double getQuantity() {
		return quantity;
	}
	
}
