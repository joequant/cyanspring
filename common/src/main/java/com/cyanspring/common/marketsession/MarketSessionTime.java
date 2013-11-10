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

import java.text.ParseException;
import java.util.Date;

import com.cyanspring.common.util.TimeUtil;

public class MarketSessionTime {
	public MarketSessionType session;
	public Date start;
	public Date end;
	private static String timeFormat = "HH:mm:ss";
	
//	public MarketSessionTime(MarketSessionType session, Date start, Date end) {
//		super();
//		this.session = session;
//		this.start = start;
//		this.end = end;
//	}
	
	public MarketSessionTime(MarketSessionType session, String start, String end) throws ParseException {
		super();
		this.session = session;
		this.start = TimeUtil.parseTime(timeFormat, start);
		this.end = TimeUtil.parseTime(timeFormat, end);
	}

	
	public static String getTimeFormat() {
		return timeFormat;
	}

	public static void setTimeFormat(String timeFormat) {
		MarketSessionTime.timeFormat = timeFormat;
	}
	
}
