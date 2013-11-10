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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeDataConverter implements IDataConverter {

	public Object fromString(String value) throws DataConvertException {
		try {
			return new SimpleDateFormat("HH:mm:ss").parse(value);
		} catch (ParseException e) {
			throw new DataConvertException("Date covert error: should be in format of 'HH:mm:ss'");
		}
	}

	public String toString(Object object) {
		return new SimpleDateFormat("HH:mm:ss").format((Date)object);
	}

}
