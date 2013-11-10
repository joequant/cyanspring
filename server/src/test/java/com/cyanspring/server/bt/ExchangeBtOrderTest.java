package com.cyanspring.server.bt;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExecType;

public abstract class ExchangeBtOrderTest implements IDownStreamListener {
	private static final Logger log = LoggerFactory
			.getLogger(ExchangeBtOrderTest.class);
	
	static ExchangeBT exchange;
	protected IDownStreamSender sender;
	
	protected List<OrderAck> orderAcks = new LinkedList<OrderAck>();
	
	class OrderAck {
		
		public OrderAck(ChildOrder order, ExecType execType, Execution execution) {
			super();
			this.order = order;
			this.execType = execType;
			this.execution = execution;
		}
		ChildOrder order;
		ExecType execType;
		Execution execution;
	}
	
	public OrderAck getLastOrderAck() {
		if(orderAcks.size() == 0)
			return null;
		
		return orderAcks.get(orderAcks.size()-1);
	}
	
	
	public OrderAck popFirstOrderAck() {
		if(orderAcks.size() == 0)
			return null;
		
		return orderAcks.remove(0);
	}
 
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		DOMConfigurator.configure("conf/log4j.xml");
		exchange = new ExchangeBT();
		exchange.init();

	}

	@Before
	public void before() {
		List<IDownStreamConnection> cons = exchange.getConnections();
		IDownStreamConnection dsCon = cons.get(0);
		sender = dsCon.setListener(this);
	}
	
	@After
	public void after() {
		exchange.reset();
		orderAcks.clear();
	}

	@Override
	public void onState(boolean on) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onOrder(ExecType execType, ChildOrder order,
			Execution execution, String message) {
		log.debug("" + execType + " : " + order + " : " + execution + " : " + message);
		orderAcks.add(new OrderAck(order.clone(), execType, execution));
	}


	@Override
	public void onError(String orderId, String message) {
		// TODO Auto-generated method stub
		
	}
	
}
