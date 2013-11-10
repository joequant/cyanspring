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
package com.cyanspring.server.upstream;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.cyanspring.common.IPlugin;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.event.IAsyncEventManager;
import com.cyanspring.common.event.IRemoteEventManager;
import com.cyanspring.common.event.order.AmendParentOrderEvent;
import com.cyanspring.common.event.order.AmendParentOrderReplyEvent;
import com.cyanspring.common.event.order.CancelParentOrderEvent;
import com.cyanspring.common.event.order.CancelParentOrderReplyEvent;
import com.cyanspring.common.event.order.EnterParentOrderEvent;
import com.cyanspring.common.event.order.EnterParentOrderReplyEvent;
import com.cyanspring.common.event.order.ParentOrderUpdateEvent;
import com.cyanspring.common.stream.IStreamAdaptor;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.upstream.IUpStreamConnection;
import com.cyanspring.common.upstream.IUpStreamListener;
import com.cyanspring.common.upstream.IUpStreamSender;
import com.cyanspring.common.upstream.UpStreamException;
import com.cyanspring.core.event.AsyncEventProcessor;


public class UpStreamManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(UpStreamManager.class);
	@Autowired 
	private IRemoteEventManager eventManager;
	ApplicationContext applicationContext;
	private List<IStreamAdaptor<IUpStreamConnection>> adaptors;
	private Map<String, IUpStreamSender> senders = Collections.synchronizedMap(new HashMap<String, IUpStreamSender>());
	private AsyncEventProcessor eventProcessor = new AsyncEventProcessor() {

		@Override
		public void subscribeToEvents() {
			for(IStreamAdaptor<IUpStreamConnection> adaptor: adaptors) {
				for(IUpStreamConnection connection: adaptor.getConnections()) {
					subscribeToEvent(ParentOrderUpdateEvent.class, connection.getId());
					subscribeToEvent(EnterParentOrderReplyEvent.class, connection.getId());
					subscribeToEvent(AmendParentOrderReplyEvent.class, connection.getId());
					subscribeToEvent(CancelParentOrderReplyEvent.class, connection.getId());
				}
			}
		}

		@Override
		public IAsyncEventManager getEventManager() {
			return eventManager;
		}
	};
	
	public UpStreamManager(List<IStreamAdaptor<IUpStreamConnection>> adaptors) {
		this.adaptors = adaptors;
	}
	
	class UpStreamListener implements IUpStreamListener {
		IUpStreamConnection connection;
		
		public UpStreamListener(IUpStreamConnection connection) {
			super();
			this.connection = connection;
		}
		
		@Override
		public void onState(boolean on) {
			if (!on) {
				log.warn("Up Stream connection is down: " + connection.getId());
			}
		}

		@Override
		public void onError(String orderId, String message) {
			log.error("Connection " + connection.getId() + " has error: orderId " + orderId + ", " + message);
		}

		@Override
		public void onNewOrder(String txId, Map<String, Object> fields) {
			log.debug("txId: " + txId + " [" + fields.toString() + "]");
			EnterParentOrderEvent event = new EnterParentOrderEvent(connection.getId(), null, fields, txId, true);
			eventManager.sendEvent(event);
		}

		@Override
		public void onAmendOrder(String txId, Map<String, Object> fields) {
			AmendParentOrderEvent event = new AmendParentOrderEvent(connection.getId(), null, fields, txId);
			eventManager.sendEvent(event);
		}

		@Override
		public void onCancelOrder(String txId, String orderId) {
			CancelParentOrderEvent event = new CancelParentOrderEvent(connection.getId(), null, orderId, txId);
			eventManager.sendEvent(event);
		}

	}
	
	@Override
	public void init() throws Exception {
		log.info("initialising");
		Map<String, Object> dupCheck = new HashMap<String, Object>();
		for(IStreamAdaptor<IUpStreamConnection> adaptor: adaptors) {
			try {
				adaptor.init();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new UpStreamException(e.getMessage());
			}
			for(IUpStreamConnection connection: adaptor.getConnections()) {
				if(dupCheck.containsKey(connection.getId()))
					throw new UpStreamException("This connection id already exists: " + connection.getId());
					
				dupCheck.put(connection.getId(), null);
			}
		}
		
		// subscribe to events
		eventProcessor.setHandler(this);
		eventProcessor.init();
		if(eventProcessor.getThread() != null)
			eventProcessor.getThread().setName("UpStreamManager");
		
		for(IStreamAdaptor<IUpStreamConnection> adaptor: adaptors) {
			for(IUpStreamConnection connection: adaptor.getConnections()) {
				UpStreamListener listener = new UpStreamListener(connection);
				IUpStreamSender sender = connection.setListener(listener);
				senders.put(connection.getId(), sender);
			}
		}
	}

	@Override
	public void uninit() {
		log.info("uninitialising");
		eventProcessor.uninit();
		senders.clear();
	}
	
	public void processEnterParentOrderReplyEvent(EnterParentOrderReplyEvent event) {
		IUpStreamSender sender = senders.get(event.getKey());
//		Accepting order is sent by ParentOrderUpdateEvent
		if(event.isOk()) {
//			sender.updateOrder(ExecType.NEW, event.getTxId(), event.getOrder(), event.getMessage());
		} else {
			sender.updateOrder(ExecType.REJECTED, event.getTxId(), event.getOrder(), event.getMessage());
		}
	}
	
	public void processAmendParentOrderReplyEvent(AmendParentOrderReplyEvent event) {
		IUpStreamSender sender = senders.get(event.getKey());
//		Accepting amendment is sent by ParentOrderUpdateEvent
		if(event.isOk()) {
//			sender.updateOrder(ExecType.REPLACE, event.getTxId(), event.getOrder(), event.getMessage());
		} else {
			sender.updateOrder(ExecType.REJECTED, event.getTxId(), event.getOrder(), event.getMessage());
		}
	}
	public void processCancelParentOrderReplyEvent(CancelParentOrderReplyEvent event) {
		IUpStreamSender sender = senders.get(event.getKey());
//		Accepting cancellation is sent by ParentOrderUpdateEvent
		if(event.isOk()) {
//			sender.updateOrder(ExecType.CANCELED, event.getTxId(), event.getOrder(), event.getMessage());
		} else {
			sender.updateOrder(ExecType.REJECTED, event.getTxId(), event.getOrder(), event.getMessage());
		}
	}
	
	public void processParentOrderUpdateEvent(ParentOrderUpdateEvent event) {
		
		IUpStreamSender sender = senders.get(event.getKey());
		if(null == sender ) {
			log.error("Can't get sender, order may have come to the wrong channel: " + event.getKey());
			return;
		}
		
		String txId = event.getOrder().get(String.class, OrderField.CLORDERID.value());
		if(null == txId) {
			log.error("ClOrdID field is null, order may have come to the wrong channel: " + event.getOrder());
			return;
		}
			
		sender.updateOrder(event.getExecType(), txId, event.getOrder(), event.getInfo());
	}
	

}
