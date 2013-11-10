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

public abstract class AsyncEvent {
	private String key;
	private EventPriority priority = EventPriority.NORMAL;;
	
	public AsyncEvent() {
	}

	public AsyncEvent(String key) {
		this();
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	@SuppressWarnings("unchecked")
	public static <T extends AsyncEvent> T getEvent(Class<T> t, AsyncEvent event) {
		if (event.getClass().equals(t)) {
			return (T)event;
		} 
		return null;
	}

	public EventPriority getPriority() {
		return priority;
	}

	public void setPriority(EventPriority priority) {
		this.priority = priority;
	}
	
	
}
