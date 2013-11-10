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
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.type.ExecType;

public class UpdateParentOrderEvent extends AsyncEvent {
	private ExecType execType;
	private String txId;
	private ParentOrder parent;
	private String info;

	public UpdateParentOrderEvent(ExecType execType, String txId,
			ParentOrder parent, String info) {
		super();
		this.execType = execType;
		this.txId = txId;
		this.parent = parent;
		this.info = info;
	}

	public ParentOrder getParent() {
		return parent;
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
