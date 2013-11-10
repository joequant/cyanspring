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
import com.cyanspring.common.util.IdGenerator;

public class NewMultiInstrumentStrategyEvent extends RemoteAsyncEvent {
	private String txId;
	private Map<String, Object> strategy;
	private List<Map<String, Object>> instruments;
	
	public NewMultiInstrumentStrategyEvent(String key, String receiver,
			Map<String, Object> strategy, List<Map<String, Object>> instruments) {
		super(key, receiver);
		this.strategy = strategy;
		this.instruments = instruments;
		this.txId = IdGenerator.getInstance().getNextID() + "T";
	}
	public Map<String, Object> getStrategy() {
		return strategy;
	}
	public List<Map<String, Object>> getInstruments() {
		return instruments;
	}
	public String getTxId() {
		return txId;
	}
}
