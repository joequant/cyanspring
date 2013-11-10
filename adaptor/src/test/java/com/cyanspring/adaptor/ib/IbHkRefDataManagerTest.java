package com.cyanspring.adaptor.ib;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.cyanspring.common.business.RefDataField;
import com.ib.client.Contract;

public class IbHkRefDataManagerTest {

	@Test
	public void test() {
		IbHkRefDataManager rm = new IbHkRefDataManager();
		assertTrue(rm.getRefData("001.HK") == null);
		assertTrue(rm.getRefData(".HK") == null);
		assertTrue(rm.getRefData("000.HK") == null);
		assertTrue(rm.getRefData("0001") != null);
		assertTrue((rm.getRefData("0001").get(Contract.class,RefDataField.CONTRACT.value())).m_symbol.equals("1"));
		assertTrue(rm.getRefData("0205") != null);
		assertTrue((rm.getRefData("0205").get(Contract.class,RefDataField.CONTRACT.value())).m_symbol.equals("205"));
		assertTrue(rm.getRefData("1205") != null);
		assertTrue((rm.getRefData("1205").get(Contract.class,RefDataField.CONTRACT.value())).m_symbol.equals("1205"));
	}
}
