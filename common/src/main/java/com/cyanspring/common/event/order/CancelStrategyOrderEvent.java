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

import com.cyanspring.common.event.EventPriority;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class CancelStrategyOrderEvent extends RemoteAsyncEvent {
	private String txId;
	String sourceId;

	public CancelStrategyOrderEvent(String key, String receiver, String txId,
			String sourceId) {
		super(key, receiver);
		setPriority(EventPriority.HIGH);
		this.txId = txId;
		this.sourceId = sourceId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getTxId() {
		return txId;
	}
	
}
