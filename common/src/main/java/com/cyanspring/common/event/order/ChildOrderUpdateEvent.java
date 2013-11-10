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

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.type.ExecType;

public class ChildOrderUpdateEvent extends RemoteAsyncEvent {
	private ExecType execType; 
	private ChildOrder order;
	private Execution execution; 
	private String message;
	
	public ChildOrderUpdateEvent(String key, String receiver,
			ExecType execType, ChildOrder order, Execution execution,
			String message) {
		super(key, receiver);
		this.execType = execType;
		this.order = order;
		this.execution = execution;
		this.message = message;
	}

	public ExecType getExecType() {
		return execType;
	}
	public ChildOrder getOrder() {
		return order;
	}
	public Execution getExecution() {
		return execution;
	}

	public String getMessage() {
		return message;
	}
}
