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
package com.cyanspring.common.business;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.util.IdGenerator;

public class MultiInstrumentStrategyData extends DataObject {
	private static final Logger log = LoggerFactory
			.getLogger(MultiInstrumentStrategyData.class);
	
	private Map<String, Instrument> instrumentData;
	
	public MultiInstrumentStrategyData(Map<String, Object> fields) {
		super(fields);
		instrumentData = new HashMap<String, Instrument>();
		put(OrderField.SERVER_ID.value(), IdGenerator.getInstance().getSystemId());
	}

	public Map<String, Instrument> getInstrumentData() {
		return instrumentData;
	}
	
	// return the first one found if there is any duplicate
	public Instrument getInstrumentBySymbol(String symbol) {
		for(Instrument instr: instrumentData.values()) {
			if(instr.getSymbol().equals(symbol))
				return instr;
		}
		return null;
	}
	
	public void setPnl(double Pnl) {
		put(OrderField.PNL.value(), Pnl);
	}
	
	public double getPnl() {
		return get(Double.TYPE, OrderField.PNL.value());
	}
	
	@Override
	public Object clone() {
		MultiInstrumentStrategyData obj = null;
		try {
			obj = (MultiInstrumentStrategyData)super.clone();
			obj.instrumentData = new HashMap<String, Instrument>();
			for(Entry<String, Instrument> entry: this.instrumentData.entrySet()) {
				Instrument instr = (Instrument)entry.getValue().clone();
				obj.instrumentData.put(entry.getKey(), instr);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return obj;
	}
	
	public String getId() {
		return this.get(String.class, OrderField.ID.value());
	}
}
