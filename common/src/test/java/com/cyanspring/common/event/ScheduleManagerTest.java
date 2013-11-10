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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScheduleManagerTest {
	class TimerListener implements IAsyncEventListener {
		public int count = 0;;
		@Override
		public void onEvent(AsyncEvent event) {
			synchronized(this) {
				this.notify();
				count++;
			}
		}
	}
	
	class FiveTimerListener implements IAsyncEventListener {
		public int count = 0;;
		@Override
		public void onEvent(AsyncEvent event) {
			count++;
			if(count == 5)
				synchronized(this) {
					this.notify();
				}
		}
	}
	
	private ScheduleManager scheduleManager = new ScheduleManager();
	@Test
	public void testOneTime() throws InterruptedException {
		TimerListener listener = new TimerListener();
		AsyncTimerEvent event = new AsyncTimerEvent();
		scheduleManager.scheduleTimerEvent(10, listener, event);
		// schedule the timer twice, but previous one is overwritten
		// so only one should fire.
		scheduleManager.scheduleTimerEvent(20, listener, event);
		synchronized(listener) {
			listener.wait(100);
		}
		Thread.sleep(50);
		assertTrue(listener.count == 1);
		
		//already auto removed by timer firing, so it should return false
		assertFalse(scheduleManager.cancelTimerEvent(event));
	}
	
	@Test
	public void testRepeat() throws InterruptedException {
		FiveTimerListener listener = new FiveTimerListener();
		AsyncTimerEvent event = new AsyncTimerEvent();
		scheduleManager.scheduleRepeatTimerEvent(10, listener, event);
		synchronized(listener) {
			listener.wait(100);
		}
		
		assertTrue(listener.count == 5);
		assertTrue(scheduleManager.cancelTimerEvent(event));
	}

}
