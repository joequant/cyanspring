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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.cyanspring.common.util.ReflectionUtil;
import com.cyanspring.common.util.TimeUtil;

@SuppressWarnings("rawtypes")
public class GenericDataConverter {
	public final static String timeFormat = "HH:mm:ss";
	public final static String dateFormat = "yyyy/MM/dd HH:mm:ss";
	private static SimpleDateFormat shortFormat = new SimpleDateFormat(timeFormat);
	private static SimpleDateFormat longFormat = new SimpleDateFormat(dateFormat);
	
	private Map<String, IDataConverter> fieldMap;

	private Map<Class, IDataConverter> typeMap;
	
	public Map<String, IDataConverter> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, IDataConverter> converterMap) {
		this.fieldMap = converterMap;
	}

	
	public Map<Class, IDataConverter> getTypeMap() {
		return typeMap;
	}

	public void setTypeMap(Map<Class, IDataConverter> typeMap) {
		this.typeMap = typeMap;
	}

	public String toString(String field, Object value) throws DataConvertException {
		if(null == value)
			return "";
		
		if (null != fieldMap) {
			IDataConverter converter = fieldMap.get(field);
			if (null != converter)
				return converter.toString(value);
		}
		
		if(null != typeMap) {
			IDataConverter converter = typeMap.get(value.getClass());
			if (null != converter)
				return converter.toString(value);
		}
		
		if(value instanceof Date) {
			Date date = (Date)value;
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int doy1 = cal.get(Calendar.DAY_OF_YEAR);
			cal.setTime(today);
			int doy2 = cal.get(Calendar.DAY_OF_YEAR);
			if(doy1 == doy2)  {// same day as today
				return shortFormat.format(date);
			} else {
				return longFormat.format(date);
			}
		}
		return value.toString();
	}
	
	@SuppressWarnings("unchecked")
	public Object fromString(Class t, String field, String value) throws DataConvertException{
		if (null == value)
			return null;
		
		if(null == t)
			throw new DataConvertException("Class t is null");
		
		if(!t.equals(String.class) && value.equals(""))
			return null;
		
		try	{
			if (null != fieldMap) {
				IDataConverter converter = fieldMap.get(field);
				if (null != converter)
					return converter.fromString(value);
			}
	
			if (null != typeMap) {
				IDataConverter converter = typeMap.get(t);
				if (null != converter)
					return converter.fromString(value);
			}
			
		} catch (NumberFormatException e) {
			throw new DataConvertException(e.getMessage());
		} catch (Exception e) {
			throw new DataConvertException(e.getMessage());
		}


		try {
			if(t.isEnum()) {
				return ReflectionUtil.callStaticMethod(t, "valueOf", new String[]{value});
			}
		} catch (IllegalArgumentException e) {
			throw new DataConvertException(e.getMessage());
		}
		
		try {
			if(t.equals(Date.class)) {
				if(value.length() == timeFormat.length())
					return TimeUtil.parseTime(timeFormat, value);
				else if(value.length() == dateFormat.length())
					return longFormat.parse(value);
				throw new DataConvertException("Unknown date format: " + value + 
						"; accepts only HH:mm:ss or yyyy/MM/dd HH:mm:ss");
			}
			
			if(t.equals(Short.class) || t.equals(Short.TYPE))
					return new Short(Short.parseShort(value));
			
			if(t.equals(Integer.class) || t.equals(Integer.TYPE))
				return new Integer(Integer.parseInt(value));
			
			if(t.equals(Long.class) || t.equals(Long.TYPE))
				return new Long(Long.parseLong(value));
			
			if(t.equals(Float.class) || t.equals(Float.TYPE))
				return new Float(Float.parseFloat(value));

			if(t.equals(Double.class) || t.equals(Double.TYPE))
				return new Double(Double.parseDouble(value));

			if(t.equals(Boolean.class) || t.equals(Boolean.TYPE))
				return new Boolean(Boolean.parseBoolean(value));

			if(t.equals(Character.class) || t.equals(Character.TYPE)) {
				if (value.length() == 0)
					return new Character('\u0000');
				else
					return new Character(value.charAt(0));
			}

			if(t.equals(Byte.class) || t.equals(Byte.TYPE))
				return new Byte(Byte.parseByte(value));
			
			if(t.equals(String.class))
				return value;
			
		} catch (NumberFormatException e) {
			throw new DataConvertException(e.getMessage());
		} catch (ParseException e) {
			throw new DataConvertException(e.getMessage());
		}

		throw new DataConvertException("Cant convert this field: " + field + ", Class: " + t);
	}
}
