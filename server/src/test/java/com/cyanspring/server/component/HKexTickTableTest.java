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
package com.cyanspring.server.component;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import webcurve.util.PriceUtils;

import com.cyanspring.common.staticdata.HKexTickTable;
import com.cyanspring.common.staticdata.ITickTable;

public class HKexTickTableTest {
//	private final static double tickTable[][] = { 
//		{0.01,		0.25,		0.001},
//		{0.25,		0.50,		0.005},
//		{0.50,		10.00,		0.010},
//		{10.00,		20.00,		0.020},
//		{20.00,		100.00,		0.050},
//		{100.00,	200.00,		0.100},
//		{200.00,	500.00,		0.200},
//		{500.00,	1000.00,	0.500},
//		{1000.00,	2000.00,	1.000},
//		{2000.00,	5000.00,	2.000},
//		{5000.00,	9995.00,	5.000}
//	};

	ITickTable tickTable = new HKexTickTable();
	
	@Test
	public void testRounded() {
		double price, result;
		price = 0.2513;
		result = tickTable.getRoundedPrice(price, true);
		assertTrue(PriceUtils.Equal(result, 0.255));
		
		result = tickTable.getRoundedPrice(price, false);
		assertTrue(PriceUtils.Equal(result, 0.25));
		
		price = 215;
		result = tickTable.getRoundedPrice(price, true);
		assertTrue(PriceUtils.Equal(result, 215));
		
		result = tickTable.getRoundedPrice(price, false);
		assertTrue(PriceUtils.Equal(result, 215));

		price = 215.1;
		result = tickTable.getRoundedPrice(price, true);
		assertTrue(PriceUtils.Equal(result, 215.2));
		
		result = tickTable.getRoundedPrice(price, false);
		assertTrue(PriceUtils.Equal(result, 215));
	}
	
	@Test 
	public void testOneTick() {
		double price, result;
		price = 13.2;
		result = tickTable.tickUp(price, true);
		assertTrue(PriceUtils.Equal(result, 13.22));
		result = tickTable.tickDown(price, false);
		assertTrue(PriceUtils.Equal(result, 13.18));
		
		price = 13.21;
		result = tickTable.tickUp(price, true);
		assertTrue(PriceUtils.Equal(result, 13.24));
		result = tickTable.tickUp(price, false);
		assertTrue(PriceUtils.Equal(result, 13.22));
		result = tickTable.tickDown(price, true);
		assertTrue(PriceUtils.Equal(result, 13.20));
		result = tickTable.tickDown(price, false);
		assertTrue(PriceUtils.Equal(result, 13.18));
	}

	@Test 
	public void testMultipleTick() {
		double price, result;
		price = 13.2;
		result = tickTable.tickUp(price, 83, true);
		assertTrue(PriceUtils.Equal(result, 14.86));
		result = tickTable.tickDown(price, 83, false);
		assertTrue(PriceUtils.Equal(result, 11.54));
	}
	
	@Test 
	public void testCrossBandTick() {
		double price, result;
		price = 19.2;
		result = tickTable.tickUp(price, 83, true);
		assertTrue(PriceUtils.Equal(result, 22.15));
		
		price = 10.8;
		result = tickTable.tickDown(price, 83, false);
		assertTrue(PriceUtils.Equal(result, 9.57));
	}

}
