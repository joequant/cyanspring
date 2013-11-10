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
package com.cyanspring.common.event.strategy;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.type.LogType;

public class StrategyLogEvent extends RemoteAsyncEvent {
	LogType logType;
	String message;
	public StrategyLogEvent(String key, String receiver, LogType logType,
			String message) {
		super(key, receiver);
		this.logType = logType;
		this.message = message;
	}
	public LogType getLogType() {
		return logType;
	}
	public String getMessage() {
		return message;
	}
}
