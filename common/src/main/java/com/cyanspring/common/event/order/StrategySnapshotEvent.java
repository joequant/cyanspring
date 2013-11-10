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

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.business.MultiInstrumentStrategyData;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.event.RemoteAsyncEvent;

public class StrategySnapshotEvent extends RemoteAsyncEvent {
	private List<ParentOrder> orders;
	private List<Instrument> instruments;
	private List<MultiInstrumentStrategyData> strategyData;

	public StrategySnapshotEvent(String key, String receiver,
			List<ParentOrder> orders, 
			List<Instrument> instruments,
			List<MultiInstrumentStrategyData> strategyData) {
		super(key, receiver);
		this.orders = orders;
		this.instruments = instruments;
		this.strategyData = strategyData;
	}

	public List<ParentOrder> getOrders() {
		return orders;
	}

	public List<MultiInstrumentStrategyData> getStrategyData() {
		return strategyData;
	}

	public List<Instrument> getInstruments() {
		return instruments;
	}
	
}
