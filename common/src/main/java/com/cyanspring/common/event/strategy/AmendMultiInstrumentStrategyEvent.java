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
package com.cyanspring.common.event.strategy;

import java.util.List;
import java.util.Map;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class AmendMultiInstrumentStrategyEvent extends RemoteAsyncEvent {
	private Map<String, Object> fields;
	private List<Map<String, Object>> instruments;
	private String txId;
	
	public AmendMultiInstrumentStrategyEvent(String key, String receiver,
			Map<String, Object> fields, List<Map<String, Object>> instruments,
			String txId) {
		super(key, receiver);
		this.fields = fields;
		this.instruments = instruments;
		this.txId = txId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public String getTxId() {
		return txId;
	}

	public List<Map<String, Object>> getInstruments() {
		return instruments;
	}

}
