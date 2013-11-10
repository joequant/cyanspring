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

import java.util.List;

import com.cyanspring.common.business.Execution;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class ExecutionSnapshotEvent extends RemoteAsyncEvent {
	List<Execution> executions;

	public ExecutionSnapshotEvent(String key, String receiver,
			List<Execution> executions) {
		super(key, receiver);
		this.executions = executions;
	}

	public List<Execution> getExecutions() {
		return executions;
	}

	public void setExecutions(List<Execution> executions) {
		this.executions = executions;
	}
	
}
