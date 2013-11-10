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
package com.cyanspring.cstw.common;

public enum ImageID {
	FILTER_ICON ("icons/filter.png"),
	EDIT_ICON("icons/gear.png"),
	NONE_EDIT_ICON("icons/gears.png"),
	TRUE_ICON("icons/true.png"),
	FALSE_ICON("icons/false.png"),
	PLUS_ICON("icons/plus.png"),
	AMEND_ICON("icons/amend.png"),
	PAUSE_ICON("icons/pause.png"),
	STOP_ICON("icons/stop.png"),
	START_ICON("icons/start.png"),
	PIN_ICON("icons/pin.png"),
	CANCEL_ICON("icons/cancel.png"),
	SAVE_ICON("icons/save.png"),
	;
	private String value;
	ImageID(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
}
