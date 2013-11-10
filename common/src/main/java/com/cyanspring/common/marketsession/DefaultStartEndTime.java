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

import com.cyanspring.common.Clock;
import com.cyanspring.common.util.TimeUtil;

public class DefaultStartEndTime {
	private Date start;
	private Date end;
	public DefaultStartEndTime(String start, String end) throws ParseException {
		this.start = TimeUtil.parseTime("HH:mm:ss", start);
		this.end = TimeUtil.parseTime("HH:mm:ss", end);
	}
	public Date getStart() {
		return start;
	}
	public Date getEnd() {
		return end;
	}
	
	public Date getStartOrNow() {
		Date now = Clock.getInstance().now();
		if(now.after(start))
			return now;
		
		return start;
	}
}
