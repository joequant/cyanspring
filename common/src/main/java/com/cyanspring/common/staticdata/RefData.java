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

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.data.DataObject;

public class RefData extends DataObject {
	
	public RefData() {
		// default lot size to 1
		put(RefDataField.LOT_SIZE.value(), new Integer(1));
	}
	
	public double roundToLots(double qty) {
		int lotSize = this.getLotSize();
		if (qty > 0)
			return ((long)(qty/lotSize)) * lotSize;
		else
			return 0;
	}
	
	// getters
	public String getSymbol() {
		return this.get(String.class, RefDataField.SYMBOL.value());
	}
	public int getLotSize() {
		return this.get(Integer.class, RefDataField.LOT_SIZE.value());
	}
	
	
}
