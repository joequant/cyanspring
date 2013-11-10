package com.cyanspring.adaptor.ib;

import static org.junit.Assert.*;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cyanspring.common.marketdata.IMarketDataListener;
import com.cyanspring.common.marketdata.MarketDataException;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.marketdata.Trade;
import com.cyanspring.common.staticdata.RefDataManager;

// set to ignore since we need IB connection to hand run this
@Ignore
@ContextConfiguration(locations = { "classpath:META-INF/spring/IbAdaptorTest.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class IbMarketDataTest implements IMarketDataListener {
	private static final Logger log = LoggerFactory
			.getLogger(IbMarketDataTest.class);
	
	@Autowired
	IbAdaptor ibAdaptor;

	@Autowired
	RefDataManager refDataManager;
	
	private int quoteCount = 0;
	private int tradeCount = 0;
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("src/main/resource/META-INF/spring/log4j.xml");
	}
	
	@Before
	public void before() throws Exception {
		refDataManager.init();
	}
	
	@Test
	public void testMarketData() {
		ibAdaptor.init();
		quoteCount = 0;
		tradeCount = 0;
		try {
			log.info("before subscribe");
			ibAdaptor.subscribeMarketData("C", this);
		} catch (MarketDataException e) {
			log.error(e.getMessage(), e);
			return;
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
		assertTrue(quoteCount>0);
		assertTrue(tradeCount>0);
		log.debug("Unsubscribe " + "C");
		ibAdaptor.unsubscribeMarketData("C", this);
		ibAdaptor.uninit();
	}
	
	@Override
	public void onState(boolean on) {
		log.debug("State: " + on);
		
	}

	@Override
	public void onQuote(Quote quote) {
		log.debug("Quote: " + quote);
		quoteCount++;
		quote.print();
	}

	@Override
	public void onTrade(Trade trade) {
		log.debug("Trade: " + trade);
		tradeCount++;
	}
}
