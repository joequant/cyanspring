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
package com.cyanspring.common.marketsession;

import java.util.HashMap;

public enum ExchangeType {
	HKEX("HK"),
	ASX("AX"),
	SGX("SI"),
	TWSE("TW"),
	TSE("T"),
	KSE("KS");
	
	static private HashMap<String, ExchangeType> map = new HashMap<String, ExchangeType>();
	static {
		for (ExchangeType type: ExchangeType.values()) {
			map.put(type.value(), type);
		}
	}
	
	private String value;
	ExchangeType(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public ExchangeType getStatus(String value) {
		return map.get(value);
	}


}
