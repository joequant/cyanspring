package com.cyanspring.common.marketdata;

import java.util.HashMap;

public enum TickField {
	TIME("time"),
	SYMBOL("symbol"),
	BID("bid"),
	ASK("ask"),
	BID_VOL("bid_vol"),
	ASK_VOL("ask_vol"),
	LAST("last"),
	LAST_VOL("last_vol"),
	HIGH("high"),
	LOW("low"),
	OPEN("open"),
	CLOSE("close"),
	TOTAL_VOL("total_vol"),
	;

	static HashMap<String, TickField> map = new HashMap<String, TickField>();
	
	private String value;
	TickField(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	static public TickField getValue(String str) {
		return map.get(str);
	}

	public static void validate() throws Exception {
		map.clear();
		for (TickField field: TickField.values()) {
			if (map.containsKey(field.value()))
				throw new Exception("Tick Field duplicated: " + field.value);
			else
				map.put(field.value(), field);
		}
		
	}
	
}
