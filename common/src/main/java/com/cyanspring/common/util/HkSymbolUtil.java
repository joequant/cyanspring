package com.cyanspring.common.util;

public class HkSymbolUtil {
	public static String localToRic(String symbol) {
		if(symbol.length() < 4)
			for(int i=symbol.length(); i<4; i++)
				symbol = "0" + symbol;
		
		symbol += ".HK";
		return symbol;
	}
	
	public static String ricToLocal(String symbol) {
		int index = symbol.indexOf(".HK");
		if(index < 0)
			return null;
		
		symbol = symbol.substring(0, index);
		index = 0;
		while(index<symbol.length()) {
			if(symbol.charAt(index) != '0')
				break;
			
			index++;
		}
		
		symbol = symbol.substring(index);
		return symbol;	
	}
}
