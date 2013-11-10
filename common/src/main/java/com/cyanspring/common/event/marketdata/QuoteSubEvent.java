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
package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;

public final class QuoteSubEvent extends RemoteAsyncEvent {
	String symbol;

	public QuoteSubEvent(String key, String receiver, String symbol) {
		super(key, receiver);
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
	
}
