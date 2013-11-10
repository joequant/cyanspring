package com.cyanspring.server.strategy;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import webcurve.exchange.Exchange;

import com.cyanspring.common.Clock;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.downstream.DownStreamManager;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.marketdata.QuoteEvent;
import com.cyanspring.common.event.test.TestScheduleManager;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.staticdata.TickTableManager;
import com.cyanspring.common.strategy.ExecuteTiming;
import com.cyanspring.common.strategy.IStrategy;
import com.cyanspring.common.strategy.IStrategyContainer;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.core.strategy.StrategyFactory;

@ContextConfiguration(locations = { "classpath:META-INFO/spring/StrategyTest.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class StrategyTest implements ApplicationContextAware {
	private static final Logger log = LoggerFactory
			.getLogger(StrategyTest.class);
	
	protected ApplicationContext applicationContext;
	
	@Autowired
	protected RefDataManager refDataManager;

	@Autowired
	protected StrategyFactory strategyFactory;
	
	@Autowired
	protected IStrategyContainer strategyContainer;
	
	@Autowired
	protected IAsyncEventManager eventManager;

	@Autowired
	protected TestScheduleManager scheduleManager;
	
	@Autowired
	protected Exchange exchange;
	
	@Autowired
	protected DownStreamManager downStreamManager;
	
	@Autowired
	protected TickTableManager tickTableManager;
	
	protected IStrategy _strategy;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	
	protected abstract DataObject createData();
	
	protected void createStrategy() {
		DataObject data = createData();
		String strategyName = data.get(String.class, OrderField.STRATEGY.value());
		try {
			_strategy = strategyFactory.createStrategy(
					strategyName, new Object[]{refDataManager, tickTableManager, data});
			_strategy.setContainer(strategyContainer);
			IDownStreamSender sender = downStreamManager.getSender();
			_strategy.setSender(sender);

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
			return;
		}
	}
	
	protected abstract void setupOrderBook();
	
	protected void timePass(long ms) {
		Date now = Clock.getInstance().now();
		now.setTime(now.getTime() + ms);
		Clock.getInstance().setManualClock(now);
		log.debug("Setting clock to: " + new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS").format(now));
		scheduleManager.fire();
		_strategy.execute(ExecuteTiming.NORMAL);
	}
	
	protected void setNow() {
		Clock.getInstance().setManualClock(new Date());
		scheduleManager.fire();
	}
	
	protected void setNow(Date date) {
		Clock.getInstance().setManualClock(date);
		scheduleManager.fire();
	}
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("src/test/resources/log4j.xml");
		Clock.getInstance().setMode(Clock.Mode.MANUAL);
	}
	
	@AfterClass
	public static void AfterClass() throws Exception {
	}
	
	@Before
	public void before() throws StrategyException {
		setNow();
		createStrategy();
		setupOrderBook();
		_strategy.init();
		_strategy.start();
	}
	
	@After
	public void after() {
		exchange.reset();
		_strategy.stop();
		_strategy.uninit();
	}

	protected void enterExchangeBuyOrder(String symbol, double price, double qty) {
		exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.BID, (int)qty, price, "", "");	
	}
	
	protected void enterExchangeSellOrder(String symbol, double price, double qty) {
		exchange.enterOrder(symbol, webcurve.common.Order.TYPE.LIMIT, 
				webcurve.common.BaseOrder.SIDE.ASK, (int)qty, price, "", "");	
	}
	
	protected void setQuote(String symbol, double bid, double bidVol, double ask, double askVol, double last) {
		setQuote(symbol, bid, bidVol, ask, askVol, last, last, last);
	}
	
	protected void setQuote(String symbol, double bid, double bidVol, double ask, double askVol, double last, double high, double low) {
		List<QtyPrice> bids = new LinkedList<QtyPrice>();
		List<QtyPrice> asks = new LinkedList<QtyPrice>();
		bids.add(new QtyPrice(bidVol, bid));
		asks.add(new QtyPrice(askVol, ask));
		Quote quote = new Quote(symbol, bids, asks);
		quote.setBid(bid);
		quote.setBidVol(bidVol);
		quote.setAsk(ask);
		quote.setAskVol(askVol);
		quote.setLast(last);
		quote.setHigh(high);
		quote.setLow(low);
		QuoteEvent quoteEvent = new QuoteEvent(symbol, null, quote);
		eventManager.sendEvent(quoteEvent);
	}
}
