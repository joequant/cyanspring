package com.cyanspring.transport;

import java.net.URISyntaxException;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Ignore;

@Ignore
public class ActiveMQProducerTest {
	// ActiveMQ configuration parameters
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = "tcp://localhost:61616";
    private boolean transacted;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;

	/**
	 * @param args
	 * @throws Exception 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException, Exception {
		ActiveMQProducerTest test = new ActiveMQProducerTest();
		test.testPubSub();
	}
	
	public void testQueue() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(transacted, ackMode);
		
		Destination dest = session.createQueue("subject1");
        MessageProducer senderProducer = session.createProducer(dest);
        senderProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		for (int i=0; i<1000; i++) {
			TextMessage txt = session.createTextMessage("hello" + i);
			senderProducer.send(txt);
		}
		session.close();
		connection.close();
	}
	
	public void testPubSub() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(transacted, ackMode);
		
		Destination dest = session.createTopic("subject1");
        MessageProducer senderProducer = session.createProducer(dest);
        senderProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		for (int i=0; i<1000; i++) {
			TextMessage txt = session.createTextMessage("hello" + i);
			senderProducer.send(txt);
		}
		session.close();
		connection.close();
	}
	
}
