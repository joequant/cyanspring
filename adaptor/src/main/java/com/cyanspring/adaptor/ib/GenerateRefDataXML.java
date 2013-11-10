package com.cyanspring.adaptor.ib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.cyanspring.common.business.RefDataField;
import com.cyanspring.common.staticdata.RefData;
import com.ib.client.Contract;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class GenerateRefDataXML extends RefData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XStream xstream = new XStream(new DomDriver());
		ArrayList<RefData> list;

		list = new ArrayList<RefData>();
		
		RefData refData;
		refData = new RefData();
		refData.put(RefDataField.SYMBOL.value(), "C");
		Contract contract = new Contract();
		contract.m_symbol = "C";
		contract.m_secType = "STK";
		contract.m_currency = "USD";
		contract.m_exchange = "SMART";
		refData.put(RefDataField.CONTRACT.value(), contract);
		list.add(refData);

		File file = new File("refdata/refData_IB_Sample.xml");
		try {
			file.createNewFile();
			FileOutputStream os = new FileOutputStream(file);
			xstream.toXML(list, os);
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
