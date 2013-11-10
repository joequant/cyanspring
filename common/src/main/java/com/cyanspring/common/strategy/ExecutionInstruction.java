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

import java.util.Date;
import java.util.Map;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.type.OrderAction;

public class ExecutionInstruction {
	private OrderAction action;
	ChildOrder order;
	Date timeStamp;
	Map<String, Object> changeFields;

	
	public ExecutionInstruction(OrderAction action,
			ChildOrder order, Map<String, Object> changeFields) {
		super();
		this.action = action;
		this.order = order;
		this.timeStamp = Clock.getInstance().now();
		this.changeFields = changeFields;
	}

	public OrderAction getAction() {
		return action;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Map<String, Object> getChangeFields() {
		return changeFields;
	}

	public void setExtraFields(Map<String, Object> changeFields) {
		this.changeFields = changeFields;
	}

	public ChildOrder getOrder() {
		return order;
	}

	public void setOrder(ChildOrder order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return 	"[" + order.getSymbol() + ", " +
			order.getSide() + ", " +
			action + ", " +
			order.getType() + ", " +
			(order==null?null:order) + ", " +
			changeFields + "]";

	}
}
