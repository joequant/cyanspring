package com.cyanspring.adaptor.ib;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.staticdata.RefDataManager;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrderSide;

//set to ignore since we need IB connection to hand run this
@Ignore
@ContextConfiguration(locations = { "classpath:META-INF/spring/IbAdaptorTest.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class IbOrderExceptionTest implements IDownStreamListener {
	private static final Logger log = LoggerFactory
			.getLogger(IbMarketDataTest.class);
	
	@Autowired
	IbAdaptor ibAdaptor;

	@Autowired
	RefDataManager refDataManager;
	
	private int orderCount = 0;
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("src/main/resource/META-INF/spring/log4j.xml");
	}
	
	@Before
	public void before() throws Exception {
		refDataManager.init();
	}
	
	@Test 
	public void testDownStream() throws InterruptedException {
		orderCount = 0;
		ibAdaptor.init();
		List<IDownStreamConnection> connections = ibAdaptor.getConnections();
		assertTrue(connections.size()>0);
		IDownStreamSender sender = connections.get(0).setListener(this);
		
		// new order
		ChildOrder order = new ChildOrder("C", OrderSide.Buy, 100, 20, ExchangeOrderType.LIMIT, "", "");
		try {
			sender.newOrder(order);
		} catch (DownStreamException e) {
			log.debug(e.getMessage(), e);
			assertTrue(false);
		}
		synchronized(order) {
			order.wait(2000);
		} 
		
		assertTrue(orderCount == 1);
		
		try {
			sender.cancelOrder(order);
		} catch (DownStreamException e) {
			log.debug(e.getMessage(), e);
			assertTrue(false);
		}
		synchronized(order) {
			order.wait(2000);
		} 
		assertTrue(orderCount == 2);
		
		Map<String, Object> changes = new HashMap<String, Object>();
		changes.put(OrderField.PRICE.value(), 21.0);
		try {
			sender.amendOrder(order, changes);
		} catch (DownStreamException e) {
			log.debug(e.getMessage(), e);
			assertTrue(false);
		}
		synchronized(order) {
			order.wait(2000);
		} 
		assertTrue(orderCount == 3);

		try {
			sender.cancelOrder(order);
		} catch (DownStreamException e) {
			log.debug(e.getMessage(), e);
			assertTrue(false);
		}
		synchronized(order) {
			order.wait(2000);
		} 
		assertTrue(orderCount == 4);

		ibAdaptor.uninit();
	}

	@Override
	public void onState(boolean on) {
		log.debug("State: " + on);
		
	}

	@Override
	public void onOrder(ExecType execType, ChildOrder order,
			Execution execution, String message) {
		log.debug("Order: " + execType + " - " + order);
		orderCount++;
		if(null != execution)
			log.debug("Execution: " + execution);
		
		synchronized(order) {
			order.notify();
		} 
	}

	@Override
	public void onError(String orderId, String message) {
		log.debug("onError: " + orderId + " - " + message);
		
	}
}
