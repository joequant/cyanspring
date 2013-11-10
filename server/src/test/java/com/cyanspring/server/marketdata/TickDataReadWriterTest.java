package com.cyanspring.server.marketdata;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketdata.ITickDataReader;
import com.cyanspring.common.marketdata.ITickDataWriter;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.QuoteDataReader;
import com.cyanspring.common.marketdata.QuoteDataWriter;
import com.cyanspring.common.marketdata.TickDataException;
import com.cyanspring.common.type.QtyPrice;

public class TickDataReadWriterTest {
	private static final Logger log = LoggerFactory
			.getLogger(TickDataReadWriterTest.class);
	ITickDataReader reader = new QuoteDataReader();
	ITickDataWriter writer = new QuoteDataWriter();
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
	}

	@Test
	public void test() throws TickDataException {
		Quote quote = new Quote("0005.HK", new LinkedList<QtyPrice>(), new LinkedList<QtyPrice>());
		quote.setBid(10);
		quote.setBidVol(11);
		quote.setAsk(12);
		quote.setAskVol(13);
		quote.setLast(10.5);
		quote.setLastVol(14);
		quote.setHigh(20);
		quote.setLow(9);
		quote.setOpen(10.3);
		quote.setClose(10.2);
		quote.setTotalVolume(30000);
		quote.getBids().add(new QtyPrice(10, 11));
		quote.getBids().add(new QtyPrice(9.8, 33));
		quote.getBids().add(new QtyPrice(9.7, 35));
		quote.getAsks().add(new QtyPrice(12, 13.2));
		quote.getAsks().add(new QtyPrice(65, 13.3));
		
		String str = writer.quoteToString(quote);
		log.info("output: " + str);
		Quote quote2 = reader.stringToQuote(str);
		
		assertTrue(quote.getSymbol().equals(quote2.getSymbol()));
		assertTrue(quote.getBid() == quote2.getBid());
		assertTrue(quote.getBidVol() == quote2.getBidVol());
		assertTrue(quote.getAsk() == quote2.getAsk());
		assertTrue(quote.getAskVol() == quote2.getAskVol());
		assertTrue(quote.getLast() == quote2.getLast());
		assertTrue(quote.getLastVol() == quote2.getLastVol());
		assertTrue(quote.getHigh() == quote2.getHigh());
		assertTrue(quote.getLow() == quote2.getLow());
		assertTrue(quote.getOpen() == quote2.getOpen());
		assertTrue(quote.getClose() == quote2.getClose());
		assertTrue(quote.getTotalVolume() == quote2.getTotalVolume());
		assertTrue(quote.getTimeStamp().equals(quote2.getTimeStamp()));
		for(int i=0; i<quote.getBids().size(); i++) {
			assertTrue(quote.getBids().get(i).price == quote2.getBids().get(i).price);
			assertTrue(quote.getBids().get(i).quantity == quote2.getBids().get(i).quantity);
		}
		for(int i=0; i<quote.getAsks().size(); i++) {
			assertTrue(quote.getAsks().get(i).price == quote2.getAsks().get(i).price);
			assertTrue(quote.getAsks().get(i).quantity == quote2.getAsks().get(i).quantity);
		}
	}
}
