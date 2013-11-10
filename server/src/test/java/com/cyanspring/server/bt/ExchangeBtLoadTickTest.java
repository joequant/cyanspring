package com.cyanspring.server.bt;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.TickDataException;
import com.cyanspring.common.marketdata.Trade;
import static org.junit.Assert.assertTrue;

public class ExchangeBtLoadTickTest implements IMarketDataListener {
	private Quote currentQuote;
	private static final Logger log = LoggerFactory
			.getLogger(ExchangeBtLoadTickTest.class);
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
	}

	@Test
	public void test() throws TickDataException, IOException, MarketDataException {
		String[] files = new String[]{
			"src/test/resources/ticks/ANZ.AX.txt",
			"src/test/resources/ticks/BHP.AX.txt",
			"src/test/resources/ticks/RIO.AX.txt",
			"src/test/resources/ticks/WBC.AX.txt",
		};
		ExchangeBT exchange = new ExchangeBT();
		exchange.init();
		exchange.subscribeMarketData(null, this);
		exchange.loadTickDataFiles(files);
		exchange.replay();
	}

	@Override
	public void onState(boolean on) {
		assertTrue(on);
	}

	@Override
	public void onQuote(Quote quote) {
		log.info("Quote: " + quote.getSymbol() + ", " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(quote.getTimeStamp()));
		if(null != currentQuote) {
			assertTrue( quote.getTimeStamp().equals(currentQuote.getTimeStamp()) ||  
					quote.getTimeStamp().after(currentQuote.getTimeStamp()) );
		}
		currentQuote = quote;
	}

	@Override
	public void onTrade(Trade trade) {
		log.info("Trade: " + trade.getSymbol() + ", " + trade.getPrice() + ", " + trade.getQuantity());
	}
}
