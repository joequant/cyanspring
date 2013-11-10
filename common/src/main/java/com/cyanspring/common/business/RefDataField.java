package com.cyanspring.common.business;

import java.util.HashMap;

public enum RefDataField {
	SYMBOL("Symbol"),
	DESC("Desc"),
	EXCHANGE("Exchange"),
	LOT_SIZE("Lot size"),
	OPEN("Open"),
	CLOSE("Close"),
	HIGH("High"),
	LOW("Low"),
	CONTRACT("Contract"),
	SINGLE_MA("single MA"),
	SHORT_MA("short MA"),
	MID_MA("mid MA"),
	LONG_MA("long MA"),
	;
	
	static HashMap<String, RefDataField> map = new HashMap<String, RefDataField>();
	
	private String value;
	RefDataField(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public RefDataField getValue(String str) {
		return map.get(str);
	}

	public static void validate() throws Exception {
		map.clear();
		for (RefDataField field: RefDataField.values()) {
			if (map.containsKey(field.value()))
				throw new Exception("RefDataField duplicated: " + field.value);
			else
				map.put(field.value(), field);
		}
		
	}


}
