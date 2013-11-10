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
package com.cyanspring.server.downstream.fix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.ClOrdID;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.HandlInst;
import quickfix.field.LastPx;
import quickfix.field.MsgType;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.fix.IFixDownStreamConnection;

public class FixDownStreamConnection implements IFixDownStreamConnection {
	private static final Logger log = LoggerFactory
			.getLogger(FixDownStreamConnection.class);
	private SessionID sessionID;
	private IDownStreamListener listener;
	private boolean on;
	// this map contains both the child order id and fix clOrderId
	private Map<String, ChildOrder> orders = Collections.synchronizedMap(new HashMap<String, ChildOrder>());
	
	public FixDownStreamConnection(SessionID sessionID) {
		this.sessionID = sessionID;
	}
	
	private String getNextClOrderId() {
		return IdGenerator.getInstance().getNextID() + "X";
	}
	
	public class FixDownStreamSender implements IDownStreamSender {
		private SessionID sessionID;

		public FixDownStreamSender(SessionID sessionID) {
			this.sessionID = sessionID;
		}

		private void sendMessage(Message message) throws DownStreamException {
	        try {
	            Session.sendToTarget(message, sessionID);
	        } catch (Exception e) {
				log.error(e.getMessage(), e);
	            e.printStackTrace();
	            throw new DownStreamException(e.getMessage());
	        }
		}

		@Override
		public void newOrder(ChildOrder order) throws DownStreamException {
			if (listener == null)
				throw new DownStreamException("Null listener");

			if (order == null)
				throw new DownStreamException("Order sent in is null");
			
			String clOrderId = getNextClOrderId();
			order.put(OrderField.CLORDERID.value(), clOrderId);
			order.setOrdStatus(OrdStatus.PENDING_NEW);
			orders.put(clOrderId, order);

			quickfix.fix42.NewOrderSingle nos;
			try {
				nos = new quickfix.fix42.NewOrderSingle(
				        new ClOrdID(clOrderId), 
				        new HandlInst('1'), 
				        new Symbol(order.getSymbol()),
				        new Side(FixUtils.toFixOrderSide(order.getSide())), 
				        new TransactTime(), 
				        new OrdType(FixUtils.toFixExchangeOrderType(order.getType()))
				    );
				if (order.getType() != ExchangeOrderType.MARKET)
					nos.set(new Price(order.getPrice()));
				
				nos.set(new OrderQty(order.getQuantity()));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
				throw new DownStreamException(e.getMessage());
			}
	        
			log.debug("New FIX order: " + order.getId() + ", " + clOrderId);
	        sendMessage(nos);
		}

		@Override
		public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			if (listener == null)
				throw new DownStreamException("Null listener");
			
			if (fields == null)
				throw new DownStreamException("amendOrder: fields sent in null");
			

			String clOrdID = order.get(String.class, OrderField.CLORDERID.value());
			ChildOrder existing = orders.get(clOrdID);
			if (null == existing) {
				throw new DownStreamException("Amend order id not found: " + order.getId());
			}
			
			if (order.getOrdStatus().isPending() || order.getOrdStatus().isCompleted()) {
				throw new DownStreamException("Amend order isn't in ready status: " + order.getId() + " - " + order.getOrdStatus());
			}

			order.setOrdStatus(OrdStatus.PENDING_REPLACE);
	        try {
				quickfix.fix42.OrderCancelReplaceRequest message = new quickfix.fix42.OrderCancelReplaceRequest(
				        new OrigClOrdID(clOrdID), 
				        new ClOrdID(getNextClOrderId()), 
				        new HandlInst('1'),
				        new Symbol(order.getSymbol()), 
				        new Side(FixUtils.toFixOrderSide(order.getSide())), 
				        new TransactTime(),
				        new OrdType(FixUtils.toFixExchangeOrderType(order.getType()))
				   );
				
				Double qty = (Double)fields.get(OrderField.QUANTITY.value());
				if (null != qty)
					message.set(new OrderQty(qty));
				
				Double price = (Double)fields.get(OrderField.PRICE.value());
				if (null != price)
					message.set(new Price(price));
					
				log.debug("Amend FIX order: " + order.getId() + ", " + clOrdID);
				sendMessage(message);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
				throw new DownStreamException("Exception caught in sending OrderCancelReplaceRequest: " + e.getMessage());
			}
		}

		@Override
		public void cancelOrder(ChildOrder order) throws DownStreamException {
			if (listener == null)
				throw new DownStreamException("Null listener");
			
			String clOrdID = order.get(String.class, OrderField.CLORDERID.value());
			ChildOrder existing = orders.get(clOrdID);
			if (null == existing) {
				throw new DownStreamException("Cancel order id not found: " + order.getId());
			}
			
			if (order.getOrdStatus().isPending() || order.getOrdStatus().isCompleted()) {
				throw new DownStreamException("Cancel order isn't in ready status: " + order.getId() + " - " + order.getOrdStatus());
			}

			order.setOrdStatus(OrdStatus.PENDING_CANCEL);
	        quickfix.fix42.OrderCancelRequest message;
			try {
				message = new quickfix.fix42.OrderCancelRequest(
				        new OrigClOrdID(clOrdID), 
				        new ClOrdID(getNextClOrderId()), 
				        new Symbol(order.getSymbol()),
				        new Side(FixUtils.toFixOrderSide(order.getSide())), 
				        new TransactTime());

				log.debug("Cancel FIX order: " + order.getId() + ", " + clOrdID);
				sendMessage(message);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
				throw new DownStreamException("Exception caught in sending OrderCancelRequest: " + e.getMessage());
			}
		}

		@Override
		public boolean getState() {
			return FixDownStreamConnection.this.on;
		}

	}
	
	@Override
	public IDownStreamSender setListener(IDownStreamListener listener) {
		this.listener = listener;
		
		if (null == listener)
			return null;
		
		try {
			listener.onState(on);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return new FixDownStreamSender(sessionID);
	}

	private void processExecutionReport(Message message, SessionID sessionId) throws FieldNotFound
	{
		if (listener == null) {
			log.error("processExecutionReport: Null listener");
			return;
		}
		
		String clOrderId;
		String newClOrderId = null;
        ExecType execType = (ExecType)message.getField(new ExecType());
		if (execType.valueEquals(quickfix.field.ExecType.REPLACE) || 
			execType.valueEquals(quickfix.field.ExecType.CANCELED) ||
			execType.valueEquals(quickfix.field.ExecType.REJECTED)) {
			if (message.isSetField(OrigClOrdID.FIELD)) {
		        clOrderId = message.getField(new OrigClOrdID()).getValue();
		        newClOrderId = message.getField(new ClOrdID()).getValue();
			} else  {// unsolicited reject/replace/canceled
				clOrderId = message.getField(new ClOrdID()).getValue();
			}
		} else {
	        clOrderId = message.getField(new ClOrdID()).getValue();
		}

		ChildOrder order;
        order = orders.get(clOrderId);
        if (null == order) {
        	String error = "Cant find order for execution report: " + message;
        	log.error(error);
        	listener.onError(clOrderId, error);
        	return;
        }
        
       	order.setCumQty(message.getDouble(quickfix.field.CumQty.FIELD));
   		order.setAvgPx(message.getDouble(quickfix.field.AvgPx.FIELD));

        if (execType.valueEquals(quickfix.field.ExecType.NEW)) {
        	order.setOrdStatus(OrdStatus.NEW);
        	order.touch();
        	listener.onOrder(com.cyanspring.common.type.ExecType.NEW, order, null, null);
        } else if (execType.valueEquals(quickfix.field.ExecType.REPLACE)) {
            orders.remove(clOrderId);
            orders.put(newClOrderId, order);
            if (message.isSetField(quickfix.field.OrderQty.FIELD)) {
            	order.setQuantity(message.getDouble(quickfix.field.OrderQty.FIELD));
            }
        	if (message.isSetField(quickfix.field.Price.FIELD)) {
        		order.setPrice(message.getDouble(quickfix.field.Price.FIELD));
        	}
        	order.setOrdStatus(OrdStatus.REPLACED);
        	order.put(OrderField.CLORDERID.value(), newClOrderId);
        	order.touch();
        	listener.onOrder(com.cyanspring.common.type.ExecType.REPLACE, order, null, null);
        } else if (execType.valueEquals(quickfix.field.OrdStatus.REJECTED)) { 
        	// most reject reason is in the text field
        	String reason = null;
        	if (message.isSetField(quickfix.field.Text.FIELD)) {
            	reason = message.getString(quickfix.field.Text.FIELD);
        	}
        	if (message.isSetField(quickfix.field.OrdStatus.FIELD)) {
        		char status = message.getField(new quickfix.field.OrdStatus()).getValue();
        		order.setOrdStatus(OrdStatus.getStatus(status));
        	}
				
        	order.touch();
        	listener.onOrder(com.cyanspring.common.type.ExecType.REJECTED, order, null, reason);
			
        }  else if (execType.valueEquals(quickfix.field.OrdStatus.CANCELED)) {
            orders.remove(clOrderId);
            orders.put(newClOrderId, order);
        	order.setOrdStatus(OrdStatus.CANCELED);
        	order.touch();
        	listener.onOrder(com.cyanspring.common.type.ExecType.CANCELED, order, null, null);
        } else if ( execType.valueEquals(quickfix.field.OrdStatus.PARTIALLY_FILLED) ||
        		execType.valueEquals(quickfix.field.OrdStatus.FILLED) ) {
			
        	String execId = message.getField(new ExecID()).getValue();
	        double execQty = message.getField(new quickfix.field.LastShares()).getValue(); //LastShares
	        double execPrice = message.getField(new LastPx()).getValue();

        	Execution exec = new Execution(order.getSymbol(), order.getSide(), execQty, execPrice, 
        			order.getId(), order.getParentOrderId(), order.getStrategyId(), execId);

        	if (message.isSetField(quickfix.field.OrdStatus.FIELD)) {
        		char status = message.getField(new quickfix.field.OrdStatus()).getValue();
        		order.setOrdStatus(OrdStatus.getStatus(status));
        	}
        	
        	order.touch();
        	listener.onOrder(com.cyanspring.common.type.ExecType.getType(execType.getValue()), order, exec, null);
        } else {
        	if (message.isSetField(quickfix.field.OrdStatus.FIELD)) {
        		char status = message.getField(new quickfix.field.OrdStatus()).getValue();
        		order.setOrdStatus(OrdStatus.getStatus(status));
        	}
        	listener.onOrder(com.cyanspring.common.type.ExecType.getType(execType.getValue()), order, null, null);
        }
        
	}
	
	private void processCancelReject(Message message, SessionID sessionId) throws FieldNotFound
	{
		if (listener == null) {
			log.error("processCancelReject: Null listener");
			return;
		}
		
        String clOrderId = message.getField(new OrigClOrdID()).getValue();
        ChildOrder order = orders.get(clOrderId);
        if (null == order) {
        	String error = "Cant find order for OrderCancelReject: " + message;
        	log.error(error);
        	listener.onError(clOrderId, error);
        	return;
        }
        
    	if (message.isSetField(quickfix.field.OrdStatus.FIELD)) {
    		char status = message.getField(new quickfix.field.OrdStatus()).getValue();
    		order.setOrdStatus(OrdStatus.getStatus(status));
    	}
    	String reason = null;
    	if (message.isSetField(quickfix.field.Text.FIELD)) {
        	reason = message.getString(quickfix.field.Text.FIELD);
    	}
    	listener.onOrder(com.cyanspring.common.type.ExecType.REJECTED, order, null, reason);
	}

	
	@Override
	public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, RejectLogon {
	}
	
	@Override
	public void fromApp(Message message, SessionID sessionID) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try {
    		MsgType msgType = new MsgType();
    		if (message.getHeader().getField(msgType).valueEquals("8")) {
                processExecutionReport(message, sessionID);
            } else if (message.getHeader().getField(msgType).valueEquals("9")) {
                processCancelReject(message, sessionID);
            } else {
            	log.warn("Fix message not handled: " + message.toString());
            }
        } catch (Exception e) {
			log.error(e.getMessage(), e);
            e.printStackTrace();
        }		
		
	}
	
	@Override
	public void onCreate(SessionID arg0) {
		log.info("Fix session created: " + arg0.toString());
		
	}
	
	@Override
	public void onLogon(SessionID arg0) {
		this.on = true;
		try {
			if(null != listener)
				listener.onState(on);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onLogout(SessionID arg0) {
		this.on = false;
		try {
			if(null != listener)
				listener.onState(on);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		
	}

	@Override
	public void toAdmin(Message arg0, SessionID arg1) {
	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
	}

	@Override
	public String getId() {
		return sessionID.toString();
	}

	@Override
	public boolean getState() {
		return on;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub
		
	}
	
}
