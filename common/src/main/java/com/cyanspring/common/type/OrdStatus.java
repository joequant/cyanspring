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
package com.cyanspring.common.type;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

public enum OrdStatus {
	NEW('0'),
	PARTIALLY_FILLED('1'),
	FILLED('2'),
	DONE_FOR_DAY('3'),
	CANCELED('4'),
	REPLACED('5'),
	PENDING_CANCEL('6'),
	STOPPED('7'),
	REJECTED('8'),
	SUSPENDED('9'),
	PENDING_NEW('A'),
	CALCULATED('B'),
	EXPIRED('C'),
	ACCEPTED_FOR_BIDDING('D'),
	PENDING_REPLACE('E');

	static private HashMap<Character, OrdStatus> map = new HashMap<Character, OrdStatus>();
	static {
		for (OrdStatus status: OrdStatus.values()) {
			map.put(status.value(), status);
		}
	}
	
	private char value;
	OrdStatus(char value) {
		this.value = value;
	}
	public char value() {
		return value;
	}
	
	static public OrdStatus getStatus(char c) {
		return map.get(c);
	}

	private final static Set<OrdStatus> openSet = Collections.unmodifiableSet(EnumSet.of(
			NEW,
			REPLACED,
			PARTIALLY_FILLED,
			SUSPENDED,
			ACCEPTED_FOR_BIDDING,
			STOPPED,
			PENDING_NEW,
			PENDING_CANCEL,
			PENDING_REPLACE
		));
	
	private final static Set<OrdStatus> readySet = Collections.unmodifiableSet(EnumSet.of(
			NEW,
			REPLACED,
			PARTIALLY_FILLED,
			SUSPENDED,
			ACCEPTED_FOR_BIDDING,
			STOPPED
		));
	
	private final static Set<OrdStatus> pendingSet = Collections.unmodifiableSet(EnumSet.of(
			PENDING_NEW,
			PENDING_CANCEL,
			PENDING_REPLACE
		));

	private final static Set<OrdStatus> completeSet = Collections.unmodifiableSet(EnumSet.of(
			CALCULATED,
			FILLED,
			DONE_FOR_DAY,
			CANCELED,
			EXPIRED,
			REJECTED
		));

    public boolean isOpen() {
        return openSet.contains(this);
    }

    public boolean isPending() {
        return pendingSet.contains(this);
    }
    
    static public OrdStatus pendingToReady(OrdStatus status) {
    	if(status.equals(PENDING_NEW))
    		return NEW;
    	else if(status.equals(PENDING_REPLACE))
    		return REPLACED;
    	else if(status.equals(PENDING_CANCEL))
    		return CANCELED;
    	else
    		return status;
    }

    public boolean isCompleted() {
        return completeSet.contains(this);
    }

    public boolean isReady() {
        return readySet.contains(this);
    }

}
