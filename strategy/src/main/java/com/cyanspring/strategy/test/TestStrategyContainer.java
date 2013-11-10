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
package com.cyanspring.strategy.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventInbox;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IAsyncExecuteEventListener;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.event.ScheduleManager;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.strategy.IStrategyContainer;

public class TestStrategyContainer implements IStrategyContainer,
		IAsyncEventListener, IAsyncEventInbox {
	private static final Logger log = LoggerFactory
			.getLogger(TestStrategyContainer.class);
	
	private static AtomicInteger nextId = new AtomicInteger(0);
	protected Map<String, IStrategy> strategies = new HashMap<String, IStrategy>();
	protected String id;

	@Autowired
	private IAsyncEventManager eventManager;

	@Autowired
	private ScheduleManager scheduleManager;

	@Override
	public void init() {
	}

	@Override
	public void uninit() {
	}
	
	public TestStrategyContainer() {
		id = "SC-" + nextId.getAndIncrement();
	}

	
	@Override
	public IAsyncEventInbox getInbox() {
		return this;
	}

	public boolean subscribe(Class<? extends AsyncEvent> clazz, String key,
			IAsyncExecuteEventListener listener) {
		eventManager.subscribe(clazz, key, listener);
		return true;
	}

	public boolean unsubscribe(Class<? extends AsyncEvent> clazz, String key,
			IAsyncExecuteEventListener listener) {
		eventManager.unsubscribe(clazz, key, listener);
		return true;
	}

	public void scheduleTimerEvent(long time, IAsyncExecuteEventListener listener,
			AsyncEvent event) {
		scheduleManager.scheduleTimerEvent(time, listener, event);
	}

	public void scheduleTimerEvent(Date time, IAsyncExecuteEventListener listener,
			AsyncEvent event) {
		scheduleManager.scheduleTimerEvent(time, listener, event);
	}

	public boolean scheduleRepeatTimerEvent(long time,
			IAsyncExecuteEventListener listener, AsyncEvent event) {
		return scheduleManager.scheduleRepeatTimerEvent(time, listener,
				event);
	}

	public boolean cancelTimerEvent(AsyncEvent event) {
		return scheduleManager.cancelTimerEvent(event);
	}

	public void sendEvent(AsyncEvent event) {
		eventManager.sendEvent(event);
	}
	
	public void sendRemoteEvent(RemoteAsyncEvent event) {
		try {
			eventManager.sendEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	public void sendLocalOrRemoteEvent(RemoteAsyncEvent event) {
		try {
			eventManager.sendEvent(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}

	@Override
	public void onEvent(AsyncEvent event) {
	}

	@Override
	public void addEvent(AsyncEvent event, IAsyncEventListener listener) {
		listener.onEvent(event);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getStrategyCount() {
		return strategies.size();
	}

}
