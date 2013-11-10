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
package com.cyanspring.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cyanspring.common.Clock;

/**
 * @author Dennis Chen
 *
 */
public class TimeUtil {
	public final static long millisInDay = 60 * 60 * 24 * 1000; 

	public static long getTimePass(Date time) {
		Date now = Clock.getInstance().now();
		return now.getTime() - time.getTime();
	}
	
	public static long getTimePass(Date now, Date time) {
		return now.getTime() - time.getTime();
	}
	
	public static Date parseTime(String format, String time) throws ParseException {
		Calendar today, adjust;
		today = Calendar.getInstance();
		today.setTime(new Date());
		adjust = Calendar.getInstance();
		adjust.setTime(new SimpleDateFormat(format).parse(time));
		adjust.set(Calendar.YEAR, today.get(Calendar.YEAR));
		adjust.set(Calendar.MONTH, today.get(Calendar.MONTH));
		adjust.set(Calendar.DATE, today.get(Calendar.DATE));
		return adjust.getTime();
		
	}

	public static Date getOnlyDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static boolean sameDate(Date d1, Date d2) {
		if(null == d1 || null == d2)
			return false;
		return getOnlyDate(d1).equals(getOnlyDate(d2));
	}
}
