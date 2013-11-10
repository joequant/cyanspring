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
package com.cyanspring.common.event;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleManager {
	Timer timer = new Timer();
	private Map<AsyncEvent, EventTimerTask> schedules = 
		Collections.synchronizedMap(new HashMap<AsyncEvent, EventTimerTask>());
	
	public void init() {
		
	}
	public void uninit() {
		
	}
	class EventTimerTask extends TimerTask
	{
		boolean once;
		IAsyncEventListener listener;
		AsyncEvent event;
		EventTimerTask(IAsyncEventListener listener, AsyncEvent event, boolean once)
		{
			super();
			this.listener = listener;
			this.event = event;
			this.once = once;
		}
		@Override
		public void run() {
			if(once)
				schedules.remove(event);
			if(listener instanceof IAsyncExecuteEventListener) {
				IAsyncExecuteEventListener al = (IAsyncExecuteEventListener)listener;
				al.getInbox().addEvent(event, listener);
			} else {
				listener.onEvent(event);
			}
		}
	}
	
	private boolean cancelIfExists(AsyncEvent event) {
		boolean result = false;
		EventTimerTask task = schedules.remove(event);
		if (task != null) {
			task.cancel();
			result = true;
		}
		return result;
	}
	
	public boolean scheduleTimerEvent(long time, IAsyncEventListener listener, AsyncEvent event)
	{
		boolean result = cancelIfExists(event);
		EventTimerTask task = new EventTimerTask(listener, event, true);
		schedules.put(event, task);
		timer.schedule(task, time);
		return result;
	}
	
	public boolean scheduleTimerEvent(Date time, IAsyncEventListener listener, AsyncEvent event)
	{
		boolean result = cancelIfExists(event);
		EventTimerTask task = new EventTimerTask(listener, event, true);
		schedules.put(event, task);
		timer.schedule(task, time);
		return result;
	}
	
	public boolean scheduleRepeatTimerEvent(long time, IAsyncEventListener listener, AsyncEvent event)
	{
		boolean result = cancelIfExists(event);
		EventTimerTask task = new EventTimerTask(listener, event, false);
		schedules.put(event, task);
		timer.scheduleAtFixedRate(task, 0, time);
		return result;
	}
	
	public boolean cancelTimerEvent(AsyncEvent event)
	{
		EventTimerTask task = schedules.remove(event);
		if ( task == null )
			return false;
		
		task.cancel();
		return true;
	}
}
