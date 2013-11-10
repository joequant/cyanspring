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
package com.cyanspring.common.staticdata;

import org.springframework.beans.factory.annotation.Autowired;

public class TickTableManager {
	@Autowired
	private ITickTable tickTable;
	
	public ITickTable getTickTable(String symbol) {
		// NOTE: if we allow trading different category of instruments with
		// different tick tables we need to load multiple tick tables and 
		// implement extra look up logic here
		return tickTable;
	}
	

}
