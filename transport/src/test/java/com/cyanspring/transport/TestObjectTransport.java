package com.cyanspring.transport;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cyanspring.common.transport.IObjectListener;
import com.cyanspring.common.transport.IObjectSender;
import com.cyanspring.common.transport.IObjectTransportService;

public class TestObjectTransport
{
	static IObjectTransportService service;
	
	
	class TestListener implements IObjectListener 
	{
		public TestObject obj;
		@Override
		public void onMessage(Object obj) {
			this.obj = (TestObject)obj;
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
		DOMConfigurator.configure("conf/log4j.xml");
		service = new ActiveMQObjectService();
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
		TestObject obj = new TestObject(TestObject.Type.Type1, "andy", 16);
		service.sendMessage("talk", obj);
		synchronized(listener) {
			listener.wait(1000);
		}

		assertTrue(listener.obj.name.equals(obj.name));
		assertTrue(listener.obj.age == obj.age);
		assertTrue(listener.obj.type.equals(obj.type));
		
		//removing listener
		listener.obj = null;
		service.removeReceiver("talk");
		IObjectSender sender = service.createObjectSender("talk");
		sender.sendMessage(obj);
		
		synchronized(listener) {
			listener.wait(1000);
		}

		assertTrue(listener.obj == null);
	}

	@Test
	public void testBasicPublishSubscribe() throws Exception {
		TestListener listener = new TestListener();
		TestListener listener2 = new TestListener();
		service.createSubscriber("broadcast", listener);
		service.createSubscriber("broadcast", listener2);
		service.createPublisher("broadcast");
		TestObject obj = new TestObject(TestObject.Type.Type1, "andy", 16);

		service.publishMessage("broadcast", obj);

		synchronized(listener) {
			listener.wait(1000);
		}
		assertTrue(listener.obj.name.equals(obj.name));
		assertTrue(listener.obj.age == obj.age);
		assertTrue(listener.obj.type.equals(obj.type));
		
		synchronized(listener2) {
			listener2.wait(1000);
		}
		assertTrue(listener2.obj.name.equals(obj.name));
		assertTrue(listener2.obj.age == obj.age);
		assertTrue(listener2.obj.type.equals(obj.type));
		
		//remove one listener
		listener.obj = null;
		service.removeSubscriber("broadcast", listener);
		IObjectSender sender = service.createObjectPublisher("broadcast");
		sender.sendMessage(obj);
		
		synchronized(listener) {
			listener.wait(1000);
		}

		assertTrue(listener.obj == null);
		
		synchronized(listener2) {
			listener2.wait(1000);
		}
		assertTrue(listener2.obj.name.equals(obj.name));
		assertTrue(listener2.obj.age == obj.age);
		assertTrue(listener2.obj.type.equals(obj.type));
	}
	

}
