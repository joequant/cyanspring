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

public enum ExecType {
//	'0' New 
//	'1' Partial fill 
//	'2' Fill 
//	'3' Done for day 
//	'4' Canceled 
//	'5' Replace 
//	'6' Pending Cancel (e.g. result of Order Cancel Request (F) )  
//	'7' Stopped 
//	'8' Rejected 
//	'9' Suspended 
//	'A' Pending New 
//	'B' Calculated 
//	'C' Expired 
//	'D' Restated ( ExecutionRpt (8) sent unsolicited by sellside, with ExecRestatementReason (378) set)  
//	'E' Pending Replace (e.g. result of Order Cancel/Replace Request (G) )  

	NEW('0'),
	PARTIALLY_FILLED('1'),
	FILLED('2'),
	DONE_FOR_DAY('3'),
	CANCELED('4'),
	REPLACE('5'),
	PENDING_CANCEL('6'),
	STOPPED('7'),
	REJECTED('8'),
	SUSPENDED('9'),
	PENDING_NEW('A'),
	CALCULATED('B'),
	EXPIRED('C'),
	RESTATED('D'),
	PENDING_REPLACE('E');
	
	static private HashMap<Character, ExecType> map = new HashMap<Character, ExecType>();
	static {
		for (ExecType type: ExecType.values()) {
			map.put(type.value(), type);
		}
	}
	
	private char value;
	ExecType(char value) {
		this.value = value;
	}
	public char value() {
		return value;
	}
	
	static public ExecType getType(char c) {
		return map.get(c);
	}

	private final static Set<ExecType> pendingSet = Collections.unmodifiableSet(EnumSet.of(
			PENDING_NEW,
			PENDING_CANCEL,
			PENDING_REPLACE
		));

	private final static Set<ExecType> completeSet = Collections.unmodifiableSet(EnumSet.of(
			CALCULATED,
			FILLED,
			DONE_FOR_DAY,
			CANCELED,
			EXPIRED,
			REJECTED
		));
	
    public boolean isPending() {
        return pendingSet.contains(this);
    }
    
    static public ExecType pendingToReady(OrdStatus status) {
    	if(status.equals(OrdStatus.PENDING_NEW))
    		return NEW;
    	else if(status.equals(OrdStatus.PENDING_REPLACE))
    		return REPLACE;
    	else if(status.equals(OrdStatus.PENDING_CANCEL))
    		return CANCELED;
    	else
    		return RESTATED;
    }
    public boolean isCompleted() {
    	return completeSet.contains(this);
    }

}
