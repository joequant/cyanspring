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

import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.type.ExecType;

public class ParentOrderUpdateEvent extends RemoteAsyncEvent {
	private ExecType execType;
	private String txId;
	private ParentOrder order;
	private String info;

	public ParentOrderUpdateEvent(String key, String receiver,
			ExecType execType, String txId, ParentOrder order, String info) {
		super(key, receiver);
		this.execType = execType;
		this.txId = txId;
		this.order = order;
		this.info = info;
	}

	public ParentOrder getOrder() {
		return order;
	}

	public ExecType getExecType() {
		return execType;
	}

	public String getTxId() {
		return txId;
	}

	public String getInfo() {
		return info;
	}
	
}
