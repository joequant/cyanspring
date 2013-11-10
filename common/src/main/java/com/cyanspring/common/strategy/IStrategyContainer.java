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
package com.cyanspring.common.strategy;

import java.util.Date;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.RemoteAsyncEvent;

public interface IStrategyContainer {
	void init();
	void uninit();
	
	String getId();
	int getStrategyCount();
	IAsyncEventInbox getInbox();

	boolean subscribe(Class<? extends AsyncEvent> clazz, String key,
			IAsyncExecuteEventListener listener);

	boolean unsubscribe(Class<? extends AsyncEvent> clazz, String key,
			IAsyncExecuteEventListener listener);

	void scheduleTimerEvent(long time, IAsyncExecuteEventListener listener,
			AsyncEvent event);

	void scheduleTimerEvent(Date time, IAsyncExecuteEventListener listener,
			AsyncEvent event);

	boolean scheduleRepeatTimerEvent(long time,
			IAsyncExecuteEventListener listener, AsyncEvent event);

	boolean cancelTimerEvent(AsyncEvent event);

	void sendEvent(AsyncEvent event);
	
	void sendRemoteEvent(RemoteAsyncEvent event);
	
	void sendLocalOrRemoteEvent(RemoteAsyncEvent event);

}
