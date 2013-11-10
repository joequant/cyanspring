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

import com.cyanspring.common.event.AsyncEvent;
import com.cyanspring.common.strategy.IStrategy;

public class AddStrategyEvent extends AsyncEvent {
	private boolean autoStart;
	private IStrategy strategy;

	public AddStrategyEvent(String key, IStrategy strategy, boolean autoStart) {
		super(key);
		this.autoStart = autoStart;
		this.strategy = strategy;
	}

	public IStrategy getStrategy() {
		return strategy;
	}

	public boolean isAutoStart() {
		return autoStart;
	}
	
}
