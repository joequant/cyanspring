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

import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AmendStrategyOrderEvent extends RemoteAsyncEvent {
	private String txId;
	private String sourceId;
	private Map<String, Object> fields;


	public AmendStrategyOrderEvent(String key, String receiver, String txId,
			String sourceId, Map<String, Object> fields) {
		super(key, receiver);
		this.txId = txId;
		this.sourceId = sourceId;
		this.fields = fields;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}

	public String getSourceId() {
		return sourceId;
	}
	
	
}
