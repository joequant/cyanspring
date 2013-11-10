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
package com.cyanspring.common.business.util;

import java.text.DecimalFormat;

public class PriceDataConverter implements IDataConverter {

	public Object fromString(String value) {
		if (value == null || value.length() == 0)
			return new Double(0);
		
		StringBuilder sb = new StringBuilder(value.trim());
		if(sb.charAt(0) == '$')
			sb.deleteCharAt(0);
		
		return Double.parseDouble(sb.toString());
	}

	public String toString(Object object) {
		if(object instanceof Double) {
			DecimalFormat format = new DecimalFormat("#0.####");
			return "$"+format.format((Double)object);
		}
			
		return object.toString();
	}

}
