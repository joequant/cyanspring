package com.cyanspring.common.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HkSymbolUtilTest {
	@Test
	public void testRic1() {
		assertTrue(HkSymbolUtil.ricToLocal("0200.HK").equals("200"));
	}
	
	@Test
	public void testRic2() {
		assertTrue(HkSymbolUtil.ricToLocal("1200.HK").equals("1200"));
	}
	
	@Test
	public void testRic3() {
		assertTrue(HkSymbolUtil.ricToLocal("1200") == null);
	}

	@Test
	public void testRic4() {
		assertTrue(HkSymbolUtil.ricToLocal("23400.HK").equals("23400"));
	}
	
	@Test
	public void testLocal1() {
		assertTrue(HkSymbolUtil.localToRic("23400").equals("23400.HK"));
	}
	
	@Test
	public void testLocal2() {
		assertTrue(HkSymbolUtil.localToRic("12").equals("0012.HK"));
	}
	
	@Test
	public void testLocal3() {
		assertTrue(HkSymbolUtil.localToRic("5").equals("0005.HK"));
	}
	
}
