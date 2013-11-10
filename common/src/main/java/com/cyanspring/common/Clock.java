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
package com.cyanspring.common;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Clock {
	public enum Mode { AUTO, MANUAL } 
	private static Clock instance;
	private Mode mode = Mode.AUTO;
	private Date manualClock = new Date(0);
	private Set<IClockListener> listeners = new HashSet<IClockListener>();
	
	private Clock() {
	}
	public static Clock getInstance() {
		if (null == instance) {
			instance = new Clock();
		}
		return instance;
	}
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	public void setManualClock(Date manualClock) {
		this.manualClock = new Date(manualClock.getTime());
		for(IClockListener listener: listeners) {
			listener.onTime(new Date(manualClock.getTime()));
		}
	}
	
	public Date now() {
		if (mode == Mode.MANUAL)
			return new Date(manualClock.getTime());
		
		return new Date();
	}
	
	public boolean addClockListener(IClockListener listener) {
		return listeners.add(listener);
	}
	
	public boolean removeClockListener(IClockListener listener) {
		return listeners.remove(listener);
	}
	
	public boolean isManual() {
		return mode.equals(Mode.MANUAL);
	}
}
