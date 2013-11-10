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
package com.cyanspring.server.upstream.fix;

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
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.CxlRejResponseTo;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrigClOrdID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.fix42.OrderCancelReject;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.upstream.IUpStreamListener;
import com.cyanspring.common.upstream.IUpStreamSender;
import com.cyanspring.common.util.DualMap;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.downstream.fix.FixConvertException;
import com.cyanspring.server.fix.FixParentOrderConverter;
import com.cyanspring.server.fix.IFixUpStreamConnection;

public class FixUpStreamConnection implements IFixUpStreamConnection {
	private static final Logger log = LoggerFactory
			.getLogger(FixUpStreamConnection.class);
	
	private FixParentOrderConverter fixParentOrderConverter;
	private DualMap<String, String> idMap = new DualMap<String, String>();
	private Map<String, Message> pendingMap = Collections.synchronizedMap(new HashMap<String, Message>());
	private SessionID sessionID;
	private IUpStreamListener listener;
	private boolean on;

	public FixUpStreamConnection(SessionID sessionID, FixParentOrderConverter fixParentOrderConverter) {
		this.fixParentOrderConverter = fixParentOrderConverter;
		this.sessionID = sessionID;
	}

	@Override
	public String getId() {
		return sessionID.toString();
	}

	@Override
	public boolean getState() {
		return on;
	}
	

	class FixUpStreamSender implements IUpStreamSender {
		private SessionID sessionID;

		public FixUpStreamSender(SessionID sessionID) {
			this.sessionID = sessionID;
		}
		
//		private void sendMessage(Message message) throws DownStreamException {
//	        try {
//	            Session.sendToTarget(message, sessionID);
//	        } catch (Exception e) {
//				log.error(e.getMessage(), e);
//	            e.printStackTrace();
//	            throw new DownStreamException(e.getMessage());
//	        }
//		}

		@Override
		public boolean getState() {
			return FixUpStreamConnection.this.on;
		}

		@Override
		public void updateOrder(com.cyanspring.common.type.ExecType execType, String txId, ParentOrder order, String info) {
			log.debug("updateOrder: " + execType + ", " + txId + ", " + order);
			
			try {
				switch(execType) {
					case NEW: 
						{
							String clOrderId = order.get(String.class, OrderField.CLORDERID.value());
							if(clOrderId != null && !clOrderId.equals(txId))	{
								log.error("txId: " + txId + " is different from ClOrdId: " + clOrderId);
								return;
							}	
							
							String id = order.getId();
							idMap.put(txId, id);
							pendingMap.remove(txId);
							quickfix.fix42.ExecutionReport er = fixParentOrderConverter.parentOrderToFix(execType, order, null);
							Session.sendToTarget(er, sessionID);
						}
						break;
					case REPLACE:
					case CANCELED:
						{
							String clOrderId = order.get(String.class, OrderField.CLORDERID.value());
							if(clOrderId != null && txId != null && !clOrderId.equals(txId))	{
								log.error("txId: " + txId + " is different from ClOrdId: " + clOrderId);
								return;
							}	
							
							String origClOrderID = null;
							if(txId != null) {
								origClOrderID = idMap.getKeyByValue(order.getId());
								if(null == origClOrderID) {
									log.error("Can't find OrigClOrderId: " + order);
									return;
								}
								pendingMap.remove(origClOrderID);
								idMap.put(txId, order.getId());
							}
							quickfix.fix42.ExecutionReport er = fixParentOrderConverter.parentOrderToFix(execType, order, origClOrderID);
							Session.sendToTarget(er, sessionID);
						}
						break;
					case REJECTED:
						{
							if(null == txId) // could be CSTW changes got rejected
								return;
							
							String clOrdID;
							if(null == order) { // new order gets rejected
								clOrdID = txId;
							} else {
								clOrdID = order.get(String.class, OrderField.CLORDERID.value());
							}
							
							Message message = pendingMap.get(clOrdID);
							if(message == null) {
								log.error("Can't find pending message with this clOrderId: " + txId + "; " + order);
								return;
							}
							String os = message.getHeader().getField(new MsgType()).getValue();
							
							if(os.equals("D")) {
								sendNewOrderReject(message, sessionID, info);
							} else if (os.equals("G")) {
								sendOrderCancelReject(message, sessionID, 
										new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST), order.getOrdStatus().value(), info);
							} else if (os.equals("F")) {
								sendOrderCancelReject(message, sessionID, 
										new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST), order.getOrdStatus().value(), info);
							} else {
								log.error("Message type unexpected: " + message);
							}
						}
						break;
					default:
						{
							quickfix.fix42.ExecutionReport er = fixParentOrderConverter.parentOrderToFix(execType, order, null);
							Session.sendToTarget(er, sessionID);
						}
						break;
				}
			} catch (FixConvertException e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			} catch (SessionNotFound e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			} catch (FieldNotFound e) {
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
			
		}
		
	}
	
	@Override
	public IUpStreamSender setListener(IUpStreamListener listener) {
		this.listener = listener;
		
		if (null == listener)
			return null;
		
		try {
			listener.onState(on);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return new FixUpStreamSender(sessionID);
	}

	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		log.info("Session " + arg1 + " Admin message: " + arg0.toString());
	}

	@Override
	public void fromApp(Message message, SessionID sessionID) throws FieldNotFound,
			IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try {
    		MsgType msgType = new MsgType();
    		if (message.getHeader().getField(msgType).valueEquals("D")) {
                processNewOrderSingle(message, sessionID);
            } else if (message.getHeader().getField(msgType).valueEquals("G")) {
                processOrderCancelReplace(message, sessionID);
            } else if (message.getHeader().getField(msgType).valueEquals("F")) {
                processOrderCancel(message, sessionID);
            } else {
            	log.warn("Fix message not handled: " + message.toString());
            }
        } catch (Exception e) {
			log.error(e.getMessage(), e);
            e.printStackTrace();
        }		
	}

	private void processOrderCancel(Message message, SessionID sessionID) {
		String clOrderId = "";
		String origClOrderId = "";
		try {
			clOrderId = message.getField(new ClOrdID()).getValue();
			origClOrderId = message.getField(new OrigClOrdID()).getValue();
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		}
		
		String orderId = idMap.get(origClOrderId);
		if(null == orderId) {
			sendOrderCancelReject(
					message, 
					sessionID, 
					new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST),
					OrdStatus.REJECTED,
					"Can't find clOrder id: " + origClOrderId);
			return;
		}
		
		if(pendingMap.containsKey(origClOrderId)) {
			sendOrderCancelReject(
					message, 
					sessionID, 
					new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REQUEST), 
					OrdStatus.REJECTED,
					"Order still in pending state: " + origClOrderId);
			return;
		}
		
		if(null == listener) {
			log.error("listener is null while receiving: " + message);
			return;
		}

		pendingMap.put(origClOrderId, message);
		listener.onCancelOrder(clOrderId, orderId);
	}

	private void sendOrderCancelReject(Message message, SessionID sessionID, CxlRejResponseTo to, char ordStatus, String error) {
		String orderID = "NONE";
	
		try {
			if(message.isSetField(new OrderID())) {
				orderID = message.getField(new OrderID()).getValue();
			}
			
			String clOrdID = message.getField(new ClOrdID()).getValue();
			String origClOrdID = message.getField(new OrigClOrdID()).getValue();
			
			quickfix.fix42.OrderCancelReject ocr = new OrderCancelReject(
				new OrderID(orderID), 
				new ClOrdID(clOrdID), 
				new OrigClOrdID(origClOrdID), 
				new OrdStatus(ordStatus), to);
			
			ocr.setField(new Text(error));
			Session.sendToTarget(ocr, sessionID);
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (SessionNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		
	}

	private void processOrderCancelReplace(Message message, SessionID sessionID) {
		String clOrderId = "";
		String origClOrderId = "";
		try {
			clOrderId = message.getField(new ClOrdID()).getValue();
			origClOrderId = message.getField(new OrigClOrdID()).getValue();
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		}
		
		String orderId = idMap.get(origClOrderId);
		if(null == orderId) {
			sendOrderCancelReject(
					message, 
					sessionID, 
					new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST), 
					OrdStatus.REJECTED,
					"Can't find clOrder id: " + origClOrderId);
			return;
		}
		
		if(pendingMap.containsKey(origClOrderId)) {
			sendOrderCancelReject(
					message, 
					sessionID, 
					new CxlRejResponseTo(CxlRejResponseTo.ORDER_CANCEL_REPLACE_REQUEST), 
					OrdStatus.REJECTED,
					"Order still in pending state: " + origClOrderId);
			return;
		}
		
		Map<String, Object> fields;
		try {
			fields = fixParentOrderConverter.fixToParentOrder(message);
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		} catch (FixConvertException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		} catch (DataConvertException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		} catch (StrategyException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		}
		
		fields.put(OrderField.ID.value(), orderId);
		
		if(null == listener) {
			log.error("listener is null while receiving: " + message);
			return;
		}
		
		pendingMap.put(origClOrderId, message);
		listener.onAmendOrder(clOrderId, fields);
	}

	private void sendNewOrderReject(Message message, SessionID sessionID, String error) {
		try {
			char side = message.getField(new Side()).getValue();
			String symbol = message.getField(new Symbol()).getValue();
			
			quickfix.fix42.ExecutionReport er = new quickfix.fix42.ExecutionReport(
				new OrderID("NONE"), 
				new ExecID(IdGenerator.getInstance().getNextID()),
				new ExecTransType(ExecTransType.NEW), 
				new ExecType(ExecType.REJECTED), 
				new OrdStatus(OrdStatus.REJECTED), 
				new Symbol(symbol), 
				new Side(side),
				new LeavesQty(), 
				new CumQty(), 
				new AvgPx() );

			if(message.isSetField(new ClOrdID())) {
				er.setField(message.getField(new ClOrdID()));
			}
			er.setField(new Text(error));
			
			Session.sendToTarget(er, sessionID);
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (SessionNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	private void processNewOrderSingle(Message message, SessionID sessionID) {
		String clOrderId = "";
		try {
			clOrderId = message.getField(new ClOrdID()).getValue();
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		}
		
		if(idMap.containsKey(clOrderId) || pendingMap.containsKey(clOrderId)) {
			sendNewOrderReject(message, sessionID, "ClOrderId duplicated: " + clOrderId);
			return;
		}
		
		Map<String, Object> fields;
		try {
			fields = 
				fixParentOrderConverter.fixToParentOrder(message);
		} catch (FieldNotFound e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			sendNewOrderReject(message, sessionID, e.getMessage());
			return;
		} catch (FixConvertException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			sendNewOrderReject(message, sessionID, e.getMessage());
			return;
		} catch (DataConvertException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			sendNewOrderReject(message, sessionID, e.getMessage());
			return;
		} catch (StrategyException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			sendNewOrderReject(message, sessionID, e.getMessage());
			return;
		}
		
		if(null == listener) {
			log.error("listener is null while receiving: " + message);
			return;
		}
		
		pendingMap.put(clOrderId, message);
		this.listener.onNewOrder(clOrderId, fields);
	}

	@Override
	public void onCreate(SessionID sessionID) {
		log.info("Fix session created: " + sessionID.toString());
	}

	@Override
	public void onLogon(SessionID sessionID) {
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
	public void onLogout(SessionID sessionID) {
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
	public void toAdmin(Message message, SessionID sessionID) {
	}

	@Override
	public void toApp(Message message, SessionID sessionID) throws DoNotSend {
	}

	@Override
	public String toString() {
		return sessionID.toString();
	}

}
