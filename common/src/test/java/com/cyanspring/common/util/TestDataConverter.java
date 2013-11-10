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
package com.cyanspring.common.util;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.business.util.IDataConverter;
import com.cyanspring.common.business.util.PriceDataConverter;
import com.cyanspring.common.type.OrderType;

public class TestDataConverter {
	public static GenericDataConverter dataConverter = new GenericDataConverter();
	@BeforeClass
	public static void BeforeClass() throws Exception {
		HashMap<String, IDataConverter> map = new HashMap<String, IDataConverter>();
		map.put("Price", new PriceDataConverter());
		dataConverter.setFieldMap(map);
	}
	
	@AfterClass
	public static void AfterClass() throws Exception {
	}

	@Test
	public void testChar() throws Exception {
		Object obj = 'a';
		assertTrue(dataConverter.toString("any", obj).equals("a"));
		assertTrue(dataConverter.fromString(char.class, "any", "a").equals(obj));
		assertTrue(dataConverter.fromString(Character.class, "any", "a").equals(obj));
	}
	
	@Test
	public void testInt() throws Exception {
		int i = 12;
		Object obj = i;
		assertTrue(dataConverter.toString("any", obj).equals("12"));
		assertTrue(dataConverter.fromString(int.class, "any", "12").equals(i));
		assertTrue(dataConverter.fromString(Integer.class, "any", "12").equals(obj));
	}
	
	@Test
	public void testBoolean() throws Exception {
		boolean b = true;
		Object obj = b;
		assertTrue(dataConverter.toString("any", obj).equals("true"));
		assertTrue(dataConverter.fromString(boolean.class, "any", "false").equals(!b));
		assertTrue(dataConverter.fromString(Boolean.class, "any", "true").equals(obj));
	}

	@Test
	public void testDouble() throws Exception {
		double i = 12.3;
		Object obj = i;
		assertTrue(dataConverter.toString("any", obj).equals("12.3"));
		assertTrue(dataConverter.fromString(double.class, "any", "12.3").equals(i));
		assertTrue(dataConverter.fromString(Double.class, "any", "12.3").equals(obj));
	}
	
	@Test
	public void testDate() throws Exception {
		// testing time convert
		Date date = new Date();
		String str = new SimpleDateFormat(GenericDataConverter.timeFormat).format(date);
		assertTrue(dataConverter.toString("any", date).equals(str));

		Date date2 = (Date)dataConverter.fromString(Date.class, "any", str);
		// they should be equal with ignoring the milliseconds
		assertTrue(date2.getTime()/1000 == date.getTime()/1000);
		
		// testing date convert
		date = new SimpleDateFormat(GenericDataConverter.timeFormat).parse(str);;
		str = new SimpleDateFormat(GenericDataConverter.dateFormat).format(date);
		assertTrue(dataConverter.toString("any", date).equals(str));
		assertTrue(dataConverter.fromString(Date.class, "any", str).equals(date));
	}
	
	@Test
	public void testPrice() throws Exception {
		Double d = 14.6;
		assertTrue(dataConverter.toString("Price", d).equals("$14.6"));
		assertTrue(dataConverter.fromString(Double.class, "Price", "14.6").equals(d));
		assertTrue(dataConverter.fromString(Double.class, "Price", "$14.6").equals(d));
	}
	
	@Test
	public void testEnum() throws Exception {
		OrderType orderType = OrderType.Market;
		assertTrue(dataConverter.toString("any", orderType).equals("Market"));
		assertTrue(dataConverter.fromString(OrderType.class, "any", "Limit").equals(OrderType.Limit));
	}
	
}
