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

import java.util.Date;

import org.junit.Test;

public class TestCallMethod {
	public int i;
	
	public TestCallMethod() {
		
	}
	public int compareTo(int i) {
		return new Integer(this.i).compareTo(i);
	}
	
	public Long compareTo(Long i) {
		return (long) new Long(this.i).compareTo(i);
	}
	
	@Test
	public void testInt() {
		TestCallMethod t1 = new TestCallMethod();
		t1.i = 2;
		Object o1 = t1;
		
		int j = 3;
		int result = ReflectionUtil.callMethod(Integer.TYPE, o1, "compareTo", new Object[]{j} );
		assertTrue(result == -1);
	}
	
	@Test
	public void testInteger() {
		Integer t1 = new Integer(2);
		Object o1 = t1;

		Integer result = ReflectionUtil.callMethod(Integer.TYPE, o1, "compareTo", new Object[]{new Integer(2)} );
		assertTrue(result == 0);
	}
	
	@Test
	public void testLong() {
		TestCallMethod t1 = new TestCallMethod();
		t1.i = 3;
		Object o1 = t1;
		
		Long j = new Long(2);
		Long result = ReflectionUtil.callMethod(Long.class, o1, "compareTo", new Object[]{j} );
		assertTrue(result == 1);
	}
	
	@Test
	public void testDate() {
		Date t1 = new Date();
		Object o1 = t1;
		Date t2 = new Date();
		t2.setTime(new Date().getTime()+1);
		
		Integer result = ReflectionUtil.callMethod(Integer.TYPE, o1, "compareTo", new Object[]{t2} );
		System.out.println(t1);
		System.out.println(t2);
		System.out.println(result);
		assertTrue(result == -1);
	}
	
	@Test
	public void testNoMethod() {
		TestCallMethod t1 = new TestCallMethod();
		t1.i = 3;
		Object o1 = t1;
		
		Long j = new Long(2);
		Long result = ReflectionUtil.callMethod(Long.class, o1, "CompareTo", new Object[]{j} );
		assertTrue(result == null);
	}
	
	@Test
	public void testMismatchedParam() {
		TestCallMethod t1 = new TestCallMethod();
		t1.i = 3;
		Object o1 = t1;
		
		Double j = new Double(2);
		Long result = ReflectionUtil.callMethod(Long.class, o1, "CompareTo", new Object[]{j} );
		assertTrue(result == null);
	}
}
