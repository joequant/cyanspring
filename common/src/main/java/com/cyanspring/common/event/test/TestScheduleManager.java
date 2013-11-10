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
package com.cyanspring.common.event.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cyanspring.common.Clock;
import com.cyanspring.common.IClockListener;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.util.TimeUtil;

public class TestScheduleManager extends ScheduleManager implements IClockListener {
	private Map<AsyncEvent, Schedule> schedules = 
		Collections.synchronizedMap(new HashMap<AsyncEvent, Schedule>());
	private List<AsyncEvent> toBeRemoved = new ArrayList<AsyncEvent>();
	
	abstract class Schedule {
		IAsyncEventListener listener;
		AsyncEvent event;
		abstract void fire();
		public Schedule(IAsyncEventListener listener, AsyncEvent event) {
			super();
			this.listener = listener;
			this.event = event;
		}
		
		protected void fireEvent() {
			if(listener instanceof IAsyncExecuteEventListener) {
				IAsyncExecuteEventListener al = (IAsyncExecuteEventListener)listener;
				al.getInbox().addEvent(event, listener);
			} else {
				listener.onEvent(event);
			}

		}
	}
	
	class TimeSchedule extends Schedule {
		long time;
		Date when;

		public TimeSchedule(IAsyncEventListener listener, AsyncEvent event,
				long time) {
			super(listener, event);
			this.time = time;
			this.when = Clock.getInstance().now();
		}

		@Override
		void fire() {
			if(TimeUtil.getTimePass(when) >= time) {
				toBeRemoved.add(event);
				fireEvent();
			}
		}
		
	}
	
	class DateSchedule extends Schedule {
		Date time;

		public DateSchedule(IAsyncEventListener listener, AsyncEvent event,
				Date time) {
			super(listener, event);
			this.time = time;
		}

		@Override
		void fire() {
			if(!Clock.getInstance().now().before(time)) {
				toBeRemoved.add(event);
				fireEvent();
			}
		}
		
	}


	class RepeatSchedule extends Schedule {
		long time;
		Date lastFired = Clock.getInstance().now();

		public RepeatSchedule(IAsyncEventListener listener, AsyncEvent event,
				long time) {
			super(listener, event);
			this.time = time;
		}

		@Override
		void fire() {
			if(TimeUtil.getTimePass(lastFired) >= time) {
				lastFired = Clock.getInstance().now();
				fireEvent();
			}
		}
		
	}
	
	public void fire() {
		for(Schedule schedule: schedules.values()) {
			schedule.fire();
		}
		
		for(AsyncEvent event: toBeRemoved) {
			schedules.remove(event);
		}
		toBeRemoved.clear();
	}
	
	@Override
	public void init() {
		super.init();
		Clock.getInstance().addClockListener(this);
	}

	@Override
	public boolean scheduleTimerEvent(long time, IAsyncEventListener listener, AsyncEvent event)
	{
		TimeSchedule schedule = new TimeSchedule(listener, event, time);
		return schedules.put(event, schedule) != null;
	}
	
	@Override
	public boolean scheduleTimerEvent(Date time, IAsyncEventListener listener, AsyncEvent event)
	{
		DateSchedule schedule = new DateSchedule(listener, event, time);
		return schedules.put(event, schedule) != null;
	}
	
	@Override
	public boolean scheduleRepeatTimerEvent(long time, IAsyncEventListener listener, AsyncEvent event)
	{
		RepeatSchedule schedule = new RepeatSchedule(listener, event, time);
		return schedules.put(event, schedule) != null;
	}
	
	@Override
	public boolean cancelTimerEvent(AsyncEvent event)
	{
		return schedules.remove(event) != null;
	}

	@Override
	public void onTime(Date time) {
		fire();
	}
}
