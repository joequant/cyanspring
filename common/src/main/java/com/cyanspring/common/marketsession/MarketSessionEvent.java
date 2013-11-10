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

import java.util.Date;

import com.cyanspring.common.event.AsyncEvent;

public class MarketSessionEvent extends AsyncEvent {
	private MarketSessionType session;
	private Date start;
	private Date end;

	public MarketSessionEvent(MarketSessionType session, Date start, Date end) {
		super();
		this.session = session;
		this.start = start;
		this.end = end;
	}

	public MarketSessionType getSession() {
		return session;
	}

	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}
	
}
