package com.cyanspring.common.marketdata;

public interface ITickDataReader {
	Quote stringToQuote(String str) throws TickDataException;
}
