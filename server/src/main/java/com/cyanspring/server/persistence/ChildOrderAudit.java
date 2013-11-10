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
package com.cyanspring.server.persistence;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.util.IdGenerator;

public class ChildOrderAudit extends ChildOrder {
	private ExecType execType; 
	private String auditId;

	public ChildOrderAudit() {
		super();
	}

	public ChildOrderAudit(ExecType execType, ChildOrder order) {
		super();
		this.auditId = IdGenerator.getInstance().getNextID() + "CA";
		this.execType = execType;
		this.update(order.getFields());
	}
	
	public ExecType getExecType() {
		return execType;
	}

	public void setExecType(ExecType execType) {
		this.execType = execType;
	}

	public String getAuditId() {
		return auditId;
	}

	public void setAuditId(String auditId) {
		this.auditId = auditId;
	}

}
