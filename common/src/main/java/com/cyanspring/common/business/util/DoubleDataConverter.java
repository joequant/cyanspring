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

public class DoubleDataConverter implements IDataConverter {

	@Override
	public Object fromString(String value) {
		Double lvalue = Double.parseDouble(value);
		return new Double(lvalue);
	}

	@Override
	public String toString(Object object) {
		if(object instanceof String)
			return (String)object;
		
		Double value = (Double)object;
		DecimalFormat format = new DecimalFormat("#0.0000");
		return format.format(value);
	}

}
