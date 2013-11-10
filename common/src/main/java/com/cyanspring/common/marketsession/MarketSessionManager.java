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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.event.IAsyncEventListener;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.ScheduleManager;

public class MarketSessionManager implements IPlugin, IAsyncEventListener {
	private static final Logger log = LoggerFactory
			.getLogger(MarketSessionManager.class);
	
	@Autowired
	private ScheduleManager scheduleManager;
	
	@Autowired
	private IRemoteEventManager eventManager;
	
	private MarketSessionType currentSessionType;
	
	private List<MarketSessionTime> timing;
	public MarketSessionManager(List<MarketSessionTime> list) {
		timing = list;
	}
	
	@Override
	public void init() {
		for(MarketSessionTime sessionTime: timing) {
			log.debug("Session start time: " + sessionTime.session + " : " + sessionTime.start);
			scheduleManager.scheduleTimerEvent(sessionTime.start, this, 
					new MarketSessionEvent(sessionTime.session, sessionTime.start, sessionTime.end));
		}
	}

	@Override
	public void uninit() {
	}

	public void onEvent(AsyncEvent event) {
		if (event instanceof MarketSessionEvent) {
			currentSessionType = ((MarketSessionEvent)event).getSession();
			eventManager.sendEvent(event);
		} else {
			log.error("unhandled event: " + event);
		}
	}

	public MarketSessionType getCurrentSessionType() {
		return currentSessionType;
	}
	
}
