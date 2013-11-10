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
package com.cyanspring.server.fix;

import java.util.Date;

import quickfix.FieldConvertError;
import quickfix.field.converter.UtcTimestampConverter;

import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.IDataConverter;

public class UtcTimeStampConverter implements IDataConverter {

	public Object fromString(String value) throws DataConvertException {
		try {
			return UtcTimestampConverter.convert(value);
		} catch (FieldConvertError e) {
			throw new DataConvertException(e.getMessage());
		}
	}

	public String toString(Object object) throws DataConvertException {
		try {
			return UtcTimestampConverter.convert((Date)object, true);
		} catch (Exception e) {
			throw new DataConvertException(e.getMessage());
		}
	}

}
