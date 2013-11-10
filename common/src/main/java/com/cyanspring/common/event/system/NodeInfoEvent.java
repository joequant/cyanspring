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
package com.cyanspring.common.event.system;

import com.cyanspring.common.event.RemoteAsyncEvent;

public class NodeInfoEvent extends RemoteAsyncEvent {
	private Boolean server;
	private Boolean firstTime;
	private String inbox;
	private String uid;

	public NodeInfoEvent(String key, String receiver, Boolean server,
			Boolean firstTime, String inbox, String uid) {
		super(key, receiver);
		this.server = server;
		this.firstTime = firstTime;
		this.inbox = inbox;
		this.uid = uid;
	}

	public Boolean getServer() {
		return server;
	}
	public Boolean getFirstTime() {
		return firstTime;
	}
	public String getInbox() {
		return inbox;
	}

	public String getUid() {
		return uid;
	}
	
}
