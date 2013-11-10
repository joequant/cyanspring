/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.transport;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.transport.IMessageListener;
import com.cyanspring.common.transport.ISender;
import com.cyanspring.common.transport.ITransportService;



public class ActiveMQService implements ITransportService, ExceptionListener {
	private static final Logger log = LoggerFactory
			.getLogger(ActiveMQService.class);
	// ActiveMQ configuration parameters
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = "tcp://localhost:61616";
    protected int persistent = DeliveryMode.NON_PERSISTENT;
    private boolean transacted;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    
    // members
    BrokerService broker;
    protected Connection connection;
    protected Session session;

    protected HashMap<String, MessageConsumer> receivers = new HashMap<String, MessageConsumer>();
    protected HashMap<String, MessageProducer> senders = new HashMap<String, MessageProducer>();  
    protected HashMap<String, MessageProducer> publishers = new HashMap<String, MessageProducer>();  
    private HashMap<String, ArrayList<IMessageListener>> subscribers = new HashMap<String, ArrayList<IMessageListener>>();
    private HashMap<IMessageListener, MessageConsumer> consumers = new HashMap<IMessageListener, MessageConsumer>();
    
    class MessageListenerAdaptor implements MessageListener {
    	
    	private IMessageListener listener;
    	public MessageListenerAdaptor (IMessageListener listener) {
    		this.listener = listener;
    	}
    	
		@Override
		public void onMessage(Message message) {
            if (message instanceof TextMessage) {
            	try {
					listener.onMessage(((TextMessage) message).getText());
				} catch (JMSException e) {
					log.error(e.getMessage(), e);
					e.printStackTrace();
				}
            } else {
            	log.error("Unexpected text message: " + message);
            }
			
		}
    	
    }
    
    class Sender implements ISender {
    	private MessageProducer producer;
    	
    	public Sender(MessageProducer producer) {
    		this.producer = producer;
    	}

		@Override
		public void sendMessage(String message) throws Exception {
			TextMessage txt = session.createTextMessage(message);
			producer.send(txt);
		}
    	
    }
    
	@Override
	public ISender createSender(String subject) throws Exception {
		MessageProducer producer = senders.get(subject);
		if (null == producer) {
			Destination dest = session.createQueue(subject);
	        producer = session.createProducer(dest);
	        producer.setDeliveryMode(persistent);
	        senders.put(subject, producer);
        }

		return new Sender(producer);
	}
	
	@Override
	public void sendMessage(String subject, String message) throws Exception {
		createSender(subject).sendMessage(message);
	}

	@Override
	public void onException(JMSException e) {
		log.error(e.getMessage(), e);
		e.printStackTrace();
	}

	@Override
	public void startBroker() throws Exception {
		broker = new BrokerService();
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI(url));
		broker.addConnector(connector);
		broker.setPersistent(false);
		broker.start();
	}

	@Override
	public void startService() throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		connection = connectionFactory.createConnection();
        connection.start();
        connection.setExceptionListener(this);
        session = connection.createSession(transacted, ackMode);
	}

	@Override
	public void createReceiver(String subject, IMessageListener listener) throws Exception {
		//only one listener per subject allowed for point to point connection
		MessageConsumer consumer = receivers.get(subject);
		if (null == consumer) {
			Destination dest = session.createQueue(subject);
			consumer = session.createConsumer(dest);
			receivers.put(subject, consumer);
		}
		if (null == listener)
			consumer.setMessageListener(null);
		else
			consumer.setMessageListener(new MessageListenerAdaptor(listener));
	}

	@Override
	public void removeReceiver(String subject) throws Exception {
		MessageConsumer consumer = receivers.get(subject);
		if (null == consumer)
			return;
		consumer.setMessageListener(null);
	}

	private void removeAllReceivers() throws JMSException {
		Iterator<Entry<String, MessageConsumer>> it = receivers.entrySet().iterator();
		 while (it.hasNext()) { 
			 Entry<String, MessageConsumer> entry = it.next();
			 entry.getValue().setMessageListener(null);
		 }
	}

	@Override
	public void publishMessage(String subject, String message) throws Exception {
		createPublisher(subject).sendMessage(message);
	}
 
	@Override
	public void closeService() throws Exception {
		removeAllReceivers();
		session.close();
		connection.close();
		
	}

	@Override
	public void closeBroker() throws Exception {
		broker.stop();
	}

	@Override
	public ISender createPublisher(String subject)
			throws Exception {
		MessageProducer producer = publishers.get(subject);
		if (null == producer) {
			Destination dest = session.createTopic(subject);
	        producer = session.createProducer(dest);
	        producer.setDeliveryMode(persistent);
	        publishers.put(subject, producer);
        }

		return new Sender(producer);
	}

	@Override
	public void createSubscriber(String subject, IMessageListener listener) throws Exception {
		//many listeners per subject allowed for publish/subscribe
		ArrayList<IMessageListener> listeners = subscribers.get(subject);
		if (null == listeners) {
			listeners = new ArrayList<IMessageListener>();
			subscribers.put(subject, listeners);
		}
		
		if (!listeners.contains(listener)) {
			Destination dest = session.createTopic(subject);
			MessageConsumer consumer = session.createConsumer(dest);
			consumer.setMessageListener(new MessageListenerAdaptor(listener));
			listeners.add(listener);
			consumers.put(listener, consumer);
		}
	}

	@Override
	public void removeSubscriber(String subject, IMessageListener listener) throws Exception {
		ArrayList<IMessageListener> listeners = subscribers.get(subject);
		if (null == listeners)
			return;

		if (listeners.contains(listener)) {
			MessageConsumer consumer = consumers.get(listener);
			if (null == consumer)
				return;
			consumer.setMessageListener(null);
			consumers.remove(listener);
			listeners.remove(listener);
		}
		
		
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPersistent() {
		return persistent;
	}

	public void setPersistent(int persistent) {
		this.persistent = persistent;
	}

	public boolean isTransacted() {
		return transacted;
	}

	public void setTransacted(boolean transacted) {
		this.transacted = transacted;
	}

	public int getAckMode() {
		return ackMode;
	}

	public void setAckMode(int ackMode) {
		this.ackMode = ackMode;
	}

}
