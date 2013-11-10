package com.cyanspring.transport;

///import junit.framework.TestCase;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cyanspring.common.transport.IMessageListener;
import com.cyanspring.common.transport.ISender;
import com.cyanspring.common.transport.ITransportService;

public class TestMessageTransport
{
	static ITransportService service;
	
	
	class TestListener implements IMessageListener 
	{
		public String message = "";
		@Override
		public void onMessage(String message) {
			this.message = message;
			//System.out.println(message);
			synchronized(this) {
				this.notify();
			}
		}
		
	}
	
	@Before
	public void Before() {
	}
	
	@After
	public void After() {
	}
	
	@BeforeClass
	public static void BeforeClass() throws Exception {
		service = new ActiveMQService();
		service.startBroker();
		service.startService();
		
	}
	
	@AfterClass
	public static void AfterClass() throws Exception {
		service.closeService();
		service.closeBroker();
	}
	
	@Test
	public void testBasicSendReceive() throws Exception {
		TestListener listener = new TestListener();
		service.createReceiver("talk", listener);
		service.createSender("talk");
		service.sendMessage("talk", "hello");
		synchronized(listener) {
			listener.wait(1000);
		}

		assertTrue(listener.message.equals("hello"));
		
		//removing listener
		listener.message = "";
		service.removeReceiver("talk");
		ISender sender = service.createSender("talk");
		sender.sendMessage("hello");
		
		synchronized(listener) {
			listener.wait(1000);
		}

		assertTrue(listener.message.equals(""));
	}

	@Test
	public void testBasicPublishSubscribe() throws Exception {
		TestListener listener = new TestListener();
		TestListener listener2 = new TestListener();
		service.createSubscriber("broadcast", listener);
		service.createSubscriber("broadcast", listener2);
		service.createPublisher("broadcast");
		service.publishMessage("broadcast", "hi there");

		synchronized(listener) {
			listener.wait(1000);
		}
		assertTrue(listener.message.equals("hi there"));
		
		synchronized(listener2) {
			listener2.wait(1000);
		}
		assertTrue(listener2.message.equals("hi there"));
		
		//remove one listener
		listener.message = "";
		service.removeSubscriber("broadcast", listener);
		ISender sender = service.createPublisher("broadcast");
		sender.sendMessage("how are you");
		
		synchronized(listener) {
			listener.wait(1000);
		}

		assertTrue(listener.message.equals(""));
		
		synchronized(listener2) {
			listener2.wait(1000);
		}
		assertTrue(listener2.message.equals("how are you"));
	}
	

}
