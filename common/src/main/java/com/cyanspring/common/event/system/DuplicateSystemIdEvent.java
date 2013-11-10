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

public class DuplicateSystemIdEvent extends RemoteAsyncEvent {
	String uid;

	public DuplicateSystemIdEvent(String key, String receiver, String uid) {
		super(key, receiver);
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}
	
}
