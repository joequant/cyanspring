package com.cyanspring.transport;

import java.net.URISyntaxException;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Ignore;

@Ignore
public class ActiveMQConsumerTest {
	// ActiveMQ configuration parameters
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = "tcp://localhost:61616";
    private boolean transacted;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;

    class MessageListenerAdaptor implements MessageListener {
    	private int id = 0;
    	public MessageListenerAdaptor(int id) {
    		this.id = id;
    	}
		@Override
		public void onMessage(Message message) {
            if (message instanceof TextMessage) {
            		try {
            			//Thread.sleep(100);
						System.out.println("Listener " + id + ": " + ((TextMessage) message).getText());
					} catch (Exception e) {
						e.printStackTrace();
					}
            } else {
            	System.out.print("Error: I dont expect none text message");
            }
			
		}
    	
    }

	/**
	 * @param args
	 * @throws Exception 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException, Exception {
//		BrokerService broker = new BrokerService();
//		TransportConnector connector = new TransportConnector();
//		connector.setUri(new URI("tcp://localhost:61616"));
//		broker.addConnector(connector);
//		broker.start();

		ActiveMQConsumerTest test = new ActiveMQConsumerTest();
		test.testPubSub();
	}
	
	public void testQueue() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(transacted, ackMode);
		
		Destination dest1 = session.createQueue("subject1");
		MessageConsumer consumer1 = session.createConsumer(dest1);
		consumer1.setMessageListener(new MessageListenerAdaptor(1));
		
		Destination dest2 = session.createQueue("subject1");
		MessageConsumer consumer2 = session.createConsumer(dest2);
		consumer2.setMessageListener(new MessageListenerAdaptor(2));
		
		MessageConsumer consumer3 = session.createConsumer(dest2);
		consumer3.setMessageListener(new MessageListenerAdaptor(3));
	}

	public void testPubSub() throws JMSException, InterruptedException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(transacted, ackMode);
		
		Destination dest1 = session.createTopic("subject1.>");
		MessageConsumer consumer1 = session.createConsumer(dest1);
		consumer1.setMessageListener(new MessageListenerAdaptor(1));
		
		Destination dest2 = session.createTopic("subject1");
		MessageConsumer consumer2 = session.createConsumer(dest2);
		consumer2.setMessageListener(new MessageListenerAdaptor(2));

		MessageConsumer consumer3 = session.createConsumer(dest2);
		consumer3.setMessageListener(new MessageListenerAdaptor(3));
		//Thread.sleep(5000);
		//consumer1.setMessageListener(null);
	}

}
