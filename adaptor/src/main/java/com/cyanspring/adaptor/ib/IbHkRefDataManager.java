package com.cyanspring.adaptor.ib;

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.staticdata.RefData;
import com.cyanspring.common.staticdata.RefDataManager;
import com.ib.client.Contract;

public class IbHkRefDataManager extends RefDataManager {
	@Override
	public RefData getRefData(String symbol) {
		RefData result = super.getRefData(symbol);
		if(null == result) {
			if(!symbol.matches("\\d+"))
				return null;
			
			int i=0;
			while(i<symbol.length()) {
				if(symbol.charAt(i) != '0')
					break;
				i++;
			}
			
			String sym = symbol.substring(i, symbol.length());
			if(sym.equals(""))
				return null;
			
			RefData ibRefData = new RefData();
			Contract contract = new Contract();
			contract.m_symbol = sym;
			contract.m_exchange = "SEHK";
			contract.m_currency = "HKD";
			contract.m_secType = "STK";
			ibRefData.put(RefDataField.CONTRACT.value(), contract);
			return ibRefData;
		}
		return result; 
	}
	
}
